package com.github.checkit.service;

import com.github.checkit.config.properties.ApplicationConfigProperties;
import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.NotificationDao;
import com.github.checkit.dao.PublicationContextDao;
import com.github.checkit.dto.NotificationDto;
import com.github.checkit.exception.ForbiddenException;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.exception.NotificationAlreadyReadException;
import com.github.checkit.model.Change;
import com.github.checkit.model.Comment;
import com.github.checkit.model.GestoringRequest;
import com.github.checkit.model.Notification;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.util.KeycloakApiUtil;
import com.github.checkit.util.NotificationTemplateUtil;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService extends BaseRepositoryService<Notification> {

    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationDao notificationDao;
    private final PublicationContextDao publicationContextDao;
    private final UserService userService;
    private final VocabularyService vocabularyService;
    private final KeycloakApiUtil keycloakApiUtil;
    private final int pageSize;

    /**
     * Constructor.
     */
    public NotificationService(NotificationDao notificationDao, PublicationContextDao publicationContextDao,
                               UserService userService,
                               VocabularyService vocabularyService, KeycloakApiUtil keycloakApiUtil,
                               ApplicationConfigProperties applicationConfigProperties) {
        this.notificationDao = notificationDao;
        this.publicationContextDao = publicationContextDao;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
        this.keycloakApiUtil = keycloakApiUtil;
        this.pageSize = applicationConfigProperties.getNotification().getPageSize();
    }

    @Override
    protected BaseDao<Notification> getPrimaryDao() {
        return notificationDao;
    }

    /**
     * Gets notifications for current user.
     *
     * @param pageNumber  page number
     * @param languageTag preferred language tag
     * @return list of notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getAllForCurrent(int pageNumber, String languageTag) {
        User current = userService.getCurrent();
        List<Notification> notifications = notificationDao.getAllForUser(current.getUri(), pageNumber, pageSize);
        return notifications.stream().map(notification -> new NotificationDto(notification, languageTag)).toList();
    }

    public int getUnreadCountForCurrent() {
        User current = userService.getCurrent();
        return notificationDao.getUnreadCountForUser(current.getUri());
    }

    /**
     * Marks specified notification as read.
     *
     * @param notificationUri URI identifier of notification
     */
    @Transactional
    public void markSeen(URI notificationUri) {
        Notification notification = findRequired(notificationUri);
        if (!notification.getAddressedTo().equals(userService.getCurrent())) {
            throw ForbiddenException.createForbiddenToMarkNotification();
        }
        if (Objects.nonNull(notification.getReadAt())) {
            throw NotificationAlreadyReadException.create(notificationUri);
        }
        notification.markRead();
        update(notification);
        logger.debug("Notification \"{}\" was marked as seen.", notificationUri);
    }

    /**
     * Marks all unread notification of current user as read.
     */
    @Transactional
    public void markAllSeen() {
        User current = userService.getCurrent();
        for (Notification notification : notificationDao.getAllUnreadForUser(current.getUri())) {
            notification.markRead();
            update(notification);
        }
        logger.debug("All unread notifications for user {} were marked as seen.", current.toSimpleString());
    }

    /**
     * Creates notifications for gestors about new publication context.
     *
     * @param publicationContext created publication context
     */
    @Transactional
    public void createdPublicationContext(PublicationContext publicationContext) {
        Set<User> gestorUsers = new HashSet<>();
        Set<URI> vocabularyURIs = new HashSet<>();
        publicationContext.getChanges().forEach(change -> vocabularyURIs.add(change.getContext().getBasedOnVersion()));
        boolean noNewVocabulary = true;
        for (URI vocabularyURI : vocabularyURIs) {
            Optional<Vocabulary> optVocabulary = vocabularyService.find(vocabularyURI);
            if (optVocabulary.isPresent()) {
                gestorUsers.addAll(optVocabulary.get().getGestors());
            } else {
                noNewVocabulary = false;
            }
        }
        Notification template = NotificationTemplateUtil.getForCreatedPublicationContext(publicationContext);
        for (User gestorUser : gestorUsers) {
            Notification notification = Notification.createFromTemplate(template);
            notification.setAddressedTo(gestorUser);
            persist(notification);
        }
        if (noNewVocabulary) {
            return;
        }
        Set<User> notifiedUsers = new HashSet<>(gestorUsers);
        template = NotificationTemplateUtil.getForCreatedPublicationContextForAdmin(publicationContext);
        for (String adminId : keycloakApiUtil.getAdminIds()) {
            User admin = userService.findRequiredByUserId(adminId);
            notifiedUsers.add(admin);
            Notification notification = Notification.createFromTemplate(template);
            notification.setAddressedTo(admin);
            persist(notification);
        }
        logger.debug("Notifications for gestors {} about created publication \"{}\", were created.",
            notifiedUsers.stream().map(User::toSimpleString).toList(), publicationContext.getUri());
    }

    /**
     * Creates notifications for editors about approved publication context.
     *
     * @param comment            Approving comment
     * @param publicationContext approved publication context
     */
    @Transactional
    public void approvedPublicationContext(Comment comment, PublicationContext publicationContext) {
        Set<User> editorUsers = new HashSet<>();
        editorUsers.add(publicationContext.getFromProject().getAuthor());
        Notification template =
            NotificationTemplateUtil.getForApprovingCommentOnPublicationContext(comment, publicationContext);
        for (User editorUser : editorUsers) {
            Notification notification = Notification.createFromTemplate(template);
            notification.setAddressedTo(editorUser);
            persist(notification);
        }
        logger.debug("Notifications for editor users {} about approved publication \"{}\" they participated in, were "
            + "created.", editorUsers.stream().map(User::toSimpleString).toList(), publicationContext.getUri());
    }

    /**
     * Creates notifications for editors about rejected publication context.
     *
     * @param comment            Rejection comment
     * @param publicationContext approved publication context
     */
    @Transactional
    public void rejectedPublicationContext(Comment comment, PublicationContext publicationContext) {
        Set<User> editorUsers = new HashSet<>();
        editorUsers.add(publicationContext.getFromProject().getAuthor());
        Notification template =
            NotificationTemplateUtil.getForRejectionCommentOnPublicationContext(comment, publicationContext);
        for (User editorUser : editorUsers) {
            Notification notification = Notification.createFromTemplate(template);
            notification.setAddressedTo(editorUser);
            persist(notification);
        }
        logger.debug("Notifications for editor users {} about rejected publication \"{}\" they participated in, were "
            + "created.", editorUsers.stream().map(User::toSimpleString).toList(), publicationContext.getUri());
    }

    /**
     * Creates notifications for gestors about new version of existing publication context.
     *
     * @param publicationContext updated publication context
     */
    @Transactional
    public void updatedPublication(PublicationContext publicationContext, Set<User> reviewers) {
        Notification template = NotificationTemplateUtil.getForUpdatedPublicationContext(publicationContext);
        for (User reviewer : reviewers) {
            Notification notification = Notification.createFromTemplate(template);
            notification.setAddressedTo(reviewer);
            persist(notification);
        }
        logger.debug("Notifications for reviewers {} about updated publication \"{}\" with their reviews, were "
            + "created.", reviewers.stream().map(User::toSimpleString).toList(), publicationContext.getUri());
    }

    /**
     * Creates notifications for involved users of change that was commented.
     *
     * @param comment created comment
     * @param change  change which was commented
     */
    @Transactional
    public void createdDiscussionComment(Comment comment, Change change) {
        PublicationContext pc = findRequiredPublicationContext(change.getUri());
        Set<User> usersToNotify = new HashSet<>();
        usersToNotify.addAll(change.getReviewBy());
        usersToNotify.addAll(userService.findAllInDiscussionOnChange(change.getUri()));
        usersToNotify.add(pc.getFromProject().getAuthor());
        usersToNotify.remove(comment.getAuthor());
        Notification template = NotificationTemplateUtil.getForDiscussionComment(comment, change, pc);
        for (User user : usersToNotify) {
            Notification notification = Notification.createFromTemplate(template);
            notification.setAddressedTo(user);
            persist(notification);
        }
        logger.debug("Notifications for users {} about created discussion comment on change \"{}\" in publication "
                + "context \"{}\", were created.", usersToNotify.stream().map(User::toSimpleString).toList(),
            change.getUri(), pc.getUri());
    }

    /**
     * Creates notifications for project creator of publication with change that was rejected with comment.
     *
     * @param comment created comment
     * @param change  change which was rejected
     */
    @Transactional
    public void createdRejectionComment(Comment comment, Change change) {
        PublicationContext pc = findRequiredPublicationContext(change.getUri());
        Set<User> gestors = new HashSet<>();
        vocabularyService.find(change.getContext().getBasedOnVersion())
            .ifPresentOrElse(voc -> gestors.addAll(voc.getGestors()),
                () -> keycloakApiUtil.getAdminIds().stream().map(userService::findRequiredByUserId)
                    .forEach(gestors::add));
        Set<User> usersToNotify = new HashSet<>(userService.findAllInDiscussionOnChange(change.getUri()));
        usersToNotify.removeAll(gestors);
        usersToNotify.add(pc.getFromProject().getAuthor());
        Notification template = NotificationTemplateUtil.getForRejectionCommentOnChange(comment, change, pc);
        for (User user : usersToNotify) {
            Notification notification = Notification.createFromTemplate(template);
            notification.setAddressedTo(user);
            persist(notification);
        }
        logger.debug("Notifications for users {} about created rejection comment on change \"{}\" in publication "
                + "context \"{}\", were created.", usersToNotify.stream().map(User::toSimpleString).toList(),
            change.getUri(), pc.getUri());
    }

    /**
     * Creates notifications for admins about new gestoring request.
     *
     * @param gestoringRequest gestoring request
     */
    @Transactional
    public void createdGestoringRequest(GestoringRequest gestoringRequest) {
        Set<User> notifiedUsers = new HashSet<>();
        Vocabulary vocabulary = vocabularyService.findRequired(gestoringRequest.getVocabulary());
        Notification template =
            NotificationTemplateUtil.getForCreatedGestoringRequest(gestoringRequest.getApplicant(), vocabulary);
        for (String adminId : keycloakApiUtil.getAdminIds()) {
            User admin = userService.findRequiredByUserId(adminId);
            notifiedUsers.add(admin);
            Notification notification = Notification.createFromTemplate(template);
            notification.setAddressedTo(admin);
            persist(notification);
        }
        logger.debug("Notifications for admins {} about new gestoring request from applicant {} for vocabulary {}, "
                + "were created.",
            notifiedUsers.stream().map(User::toSimpleString).toList(), gestoringRequest.getApplicant().toSimpleString(),
            vocabulary.toSimpleString());
    }

    /**
     * Creates notification about resolved gestoring request for applicant.
     *
     * @param gestoringRequest gestoring request
     * @param approved         if it was approved or not
     */
    @Transactional
    public void resolvedGestoringRequest(GestoringRequest gestoringRequest, boolean approved) {
        Vocabulary vocabulary = vocabularyService.findRequired(gestoringRequest.getVocabulary());
        Notification notification =
            NotificationTemplateUtil.getForResolvedGestoringRequest(vocabulary, approved);
        notification.setAddressedTo(gestoringRequest.getApplicant());
        persist(notification);
        logger.debug("Notification about {} of applicant's {} gestoring request for vocabulary {}, was created.",
            approved ? "approval" : "rejection", gestoringRequest.getApplicant().toSimpleString(),
            vocabulary.toSimpleString());
    }

    private PublicationContext findRequiredPublicationContext(URI changeUri) {
        return publicationContextDao.findFromChange(changeUri).orElseThrow(
            () -> new NotFoundException("Publication context of change \"%s\" not found.", changeUri));
    }
}
