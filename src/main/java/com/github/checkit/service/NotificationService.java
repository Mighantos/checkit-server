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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService extends BaseRepositoryService<Notification> {

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
        template = NotificationTemplateUtil.getForCreatedPublicationContextForAdmin(publicationContext);
        for (String adminId : keycloakApiUtil.getAdminIds()) {
            Notification notification = Notification.createFromTemplate(template);
            notification.setAddressedTo(userService.findRequiredByUserId(adminId));
            persist(notification);
        }
    }

    /**
     * Creates notifications for editors about approved publication context.
     *
     * @param comment            Approving comment
     * @param publicationContext approved publication context
     */
    @Transactional
    public void approvedPublicationContext(Comment comment, PublicationContext publicationContext) {
        Set<User> gestorUsers = new HashSet<>();
        gestorUsers.add(publicationContext.getFromProject().getAuthor());
        Notification template =
            NotificationTemplateUtil.getForApprovingCommentOnPublicationContext(comment, publicationContext);
        for (User gestorUser : gestorUsers) {
            Notification notification = Notification.createFromTemplate(template);
            notification.setAddressedTo(gestorUser);
            persist(notification);
        }
    }

    /**
     * Creates notifications for editors about rejected publication context.
     *
     * @param comment            Rejection comment
     * @param publicationContext approved publication context
     */
    @Transactional
    public void rejectedPublicationContext(Comment comment, PublicationContext publicationContext) {
        Set<User> gestorUsers = new HashSet<>();
        gestorUsers.add(publicationContext.getFromProject().getAuthor());
        Notification template =
            NotificationTemplateUtil.getForRejectionCommentOnPublicationContext(comment, publicationContext);
        for (User gestorUser : gestorUsers) {
            Notification notification = Notification.createFromTemplate(template);
            notification.setAddressedTo(gestorUser);
            persist(notification);
        }
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
    }

    private PublicationContext findRequiredPublicationContext(URI changeUri) {
        return publicationContextDao.findFromChange(changeUri).orElseThrow(
            () -> new NotFoundException("Publication context of change \"%s\" not found.", changeUri));
    }
}
