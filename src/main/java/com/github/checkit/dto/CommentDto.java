package com.github.checkit.dto;

import com.github.checkit.model.Comment;
import com.github.checkit.model.auxilary.CommentTag;
import java.net.URI;
import java.time.Instant;
import lombok.Getter;

@Getter
public class CommentDto {
    private final URI uri;
    private final URI topic;
    private final UserDto author;
    private final Instant creationDate;
    private final Instant lastModificationDate;
    private final CommentTag tag;
    private final String content;

    /**
     * Constructor.
     */
    public CommentDto(Comment comment) {
        this.uri = comment.getUri();
        this.topic = comment.getTopic().getUri();
        this.author = new UserDto(comment.getAuthor());
        this.creationDate = comment.getCreated();
        this.lastModificationDate = comment.getModified();
        this.tag = comment.getTag();
        this.content = comment.getContent();
    }
}
