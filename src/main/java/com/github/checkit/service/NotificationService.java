package com.github.checkit.service;

import com.github.checkit.config.properties.ApplicationConfigProperties;
import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.NotificationDao;
import com.github.checkit.dto.NotificationDto;
import com.github.checkit.exception.ForbiddenException;
import com.github.checkit.model.Notification;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.util.KeycloakApiUtil;
import com.github.checkit.util.NotificationTemplateUtil;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService extends BaseRepositoryService<Notification> {

    private final NotificationDao notificationDao;
    private final UserService userService;
    private final VocabularyService vocabularyService;
    private final CommentService commentService;
    private final KeycloakApiUtil keycloakApiUtil;
    private final int pageSize;

    /**
     * Constructor.
     */
    public NotificationService(NotificationDao notificationDao, UserService userService,
                               VocabularyService vocabularyService, CommentService commentService,
                               KeycloakApiUtil keycloakApiUtil,
                               ApplicationConfigProperties applicationConfigProperties) {
        this.notificationDao = notificationDao;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
        this.commentService = commentService;
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
        notification.markRead();
        update(notification);
    }

    /**
     * Creates notifications for gestors about new publication context.
     *
     * @param publicationContext publication context
     */
    @Transactional
    public void createdPublication(PublicationContext publicationContext) {
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
     * Creates notifications for gestors about new version of existing publication context.
     *
     * @param publicationContext publication context
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
}
