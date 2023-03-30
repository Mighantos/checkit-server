package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.CommentDao;
import com.github.checkit.dto.CommentDto;
import com.github.checkit.model.Change;
import com.github.checkit.model.Comment;
import java.net.URI;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService extends BaseRepositoryService<Comment> {

    private final CommentDao commentDao;
    private final UserService userService;
    private final ChangeService changeService;

    /**
     * Constructor.
     */
    public CommentService(CommentDao commentDao, UserService userService, ChangeService changeService) {
        this.commentDao = commentDao;
        this.userService = userService;
        this.changeService = changeService;
    }

    @Override
    protected BaseDao<Comment> getPrimaryDao() {
        return commentDao;
    }

    @Transactional
    public List<CommentDto> getAllRelatedToChange(URI changeUri) {
        changeService.getRequiredReference(changeUri);
        return commentDao.findAllRelatedToChange(changeUri).stream().sorted().map(CommentDto::new).toList();
    }

    /**
     * Creates comment related to specified change with given content.
     *
     * @param changeUri URI identifier of change
     * @param content   text content of comment
     */
    @Transactional
    public void createComment(URI changeUri, String content) {
        Change change = changeService.getRequiredReference(changeUri);
        Comment comment = new Comment();
        comment.setAuthor(userService.getCurrent());
        comment.setContent(content);
        comment.setTopic(change);
        persist(comment);
    }
}
