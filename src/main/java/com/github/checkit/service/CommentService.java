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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService extends BaseRepositoryService<Comment> {

    private final CommentDao commentDao;
    private final UserService userService;
    private final ChangeService changeService;
    private final int minimalRejectionCommentLength;

    /**
     * Constructor.
     */
    public CommentService(CommentDao commentDao, UserService userService, ChangeService changeService,
                          ApplicationConfigProperties applicationConfigProperties) {
        this.commentDao = commentDao;
        this.userService = userService;
        this.changeService = changeService;
        this.minimalRejectionCommentLength =
            applicationConfigProperties.getComment().getRejectionMinimalContentLength();

    }

    @Override
    protected BaseDao<Comment> getPrimaryDao() {
        return commentDao;
    }

    @Transactional
    public List<CommentDto> getAllRelatedToChange(URI changeUri) {
        changeService.getRequiredReference(changeUri);
        return commentDao.findAllRelatedToChange(changeUri).stream().map(CommentDto::new).toList();
    }

    /**
     * Creates discussion comment related to specified change with given content.
     *
     * @param changeUri URI identifier of change
     * @param content   text content of comment
     */
    @Transactional
    public void createComment(URI changeUri, String content) {
        Change change = changeService.getRequiredReference(changeUri);
        Comment comment = new Comment();
        comment.setTag(CommentTag.DISCUSSION);
        comment.setAuthor(userService.getCurrent());
        comment.setContent(content);
        comment.setTopic(change);
        persist(comment);
    }

    /**
     * Creates a rejection comment on specified change with given content.
     *
     * @param changeUri URI identifier of change
     * @param content   text content of comment
     */
    @Transactional
    public void createRejectionComment(URI changeUri, String content) {
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

    public void removeAllFinalComments(Change change) {
        findAllFinalComments(change).forEach(this::remove);
    }

    public void removeFinalComment(PublicationContext publicationContext) {
        findFinalComment(publicationContext).ifPresent(this::remove);
    }
}
