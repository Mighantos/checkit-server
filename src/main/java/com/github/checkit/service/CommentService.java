package com.github.checkit.service;

import com.github.checkit.config.properties.ApplicationConfigProperties;
import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.CommentDao;
import com.github.checkit.dto.CommentDto;
import com.github.checkit.exception.AlreadyExistsException;
import com.github.checkit.exception.ChangeNotRejectedException;
import com.github.checkit.exception.RejectionCommentTooShortException;
import com.github.checkit.model.Change;
import com.github.checkit.model.Comment;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.auxilary.CommentTag;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService extends BaseRepositoryService<Comment> {

    private final Logger logger = LoggerFactory.getLogger(AdminUserService.class);

    private final CommentDao commentDao;
    private final UserService userService;
    private final ChangeService changeService;
    private final NotificationService notificationService;
    private final int minimalRejectionCommentLength;

    /**
     * Constructor.
     */
    public CommentService(CommentDao commentDao, UserService userService, ChangeService changeService,
                          NotificationService notificationService,
                          ApplicationConfigProperties applicationConfigProperties) {
        this.commentDao = commentDao;
        this.userService = userService;
        this.changeService = changeService;
        this.notificationService = notificationService;
        this.minimalRejectionCommentLength =
            applicationConfigProperties.getComment().getRejectionMinimalContentLength();

    }

    @Override
    protected BaseDao<Comment> getPrimaryDao() {
        return commentDao;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> findAllInDiscussionRelatedToChange(URI changeUri) {
        changeService.getRequiredReference(changeUri);
        return commentDao.findAllRelatedToChange(changeUri).stream().map(CommentDto::new).toList();
    }

    public List<Comment> findAllFinalComments(Change change) {
        return commentDao.findAllFinalComments(change.getUri());
    }

    public Optional<Comment> findFinalComment(Change change, User user) {
        return commentDao.findFinalComment(change, user);
    }

    public Optional<Comment> findFinalComment(PublicationContext pc) {
        return commentDao.findFinalComment(pc);
    }

    /**
     * Creates discussion comment related to specified change with given content.
     *
     * @param changeUri URI identifier of change
     * @param content   text content of comment
     * @return URI identifier of the new comment
     */
    @Transactional
    public URI createDiscussionComment(URI changeUri, String content) {
        Change change = changeService.getRequiredReference(changeUri);
        User current = userService.getCurrent();
        Comment comment = new Comment();
        comment.setTag(CommentTag.DISCUSSION);
        comment.setAuthor(current);
        comment.setContent(content);
        comment.setTopic(change);
        persist(comment);
        notificationService.createdDiscussionComment(comment, change);
        logger.info("User {} created discussion comment on change \"{}\".", current.toSimpleString(), changeUri);
        return comment.getUri();
    }

    /**
     * Creates a rejection comment on specified change with given content.
     *
     * @param changeUri URI identifier of change
     * @param content   text content of comment
     * @return URI identifier of the new comment
     */
    @Transactional
    public URI createRejectionComment(URI changeUri, String content) {
        Change change = changeService.getRequiredReference(changeUri);
        User current = userService.getCurrent();
        changeService.checkUserCanReviewChange(current.getUri(), change.getUri());
        if (!change.isRejected(current)) {
            throw ChangeNotRejectedException.create(change, current);
        }
        if (findFinalComment(change, current).isPresent()) {
            throw new AlreadyExistsException("Final comment of user \"%s\" on change \"%s\" already exists.",
                current.getUri(), change.getUri());
        }
        if (content.length() < minimalRejectionCommentLength) {
            throw RejectionCommentTooShortException.create(minimalRejectionCommentLength);
        }
        Comment comment = new Comment();
        comment.setTag(CommentTag.REJECTION);
        comment.setAuthor(current);
        comment.setContent(content);
        comment.setTopic(change);
        persist(comment);
        notificationService.createdRejectionComment(comment, change);
        logger.info("User {} created rejection comment on change \"{}\".", current.toSimpleString(), changeUri);
        return comment.getUri();
    }

    public void removeFinalComment(PublicationContext publicationContext) {
        findFinalComment(publicationContext).ifPresent(this::remove);
    }

    public int getDiscussionCommentsCount(Change change) {
        return commentDao.getDiscussionCommentsCount(change.getUri());
    }
}
