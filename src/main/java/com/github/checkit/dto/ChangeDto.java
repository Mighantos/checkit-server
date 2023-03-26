package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.User;
import java.net.URI;
import lombok.Getter;

@Getter
public class ChangeDto implements Comparable<ChangeDto> {

    private final String id;
    private final URI uri;
    private final ChangeType type;
    private final String label;
    private final URI subject;
    private final URI predicate;
    private final String object;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String newObject;
    private final boolean approved;
    private final boolean rejected;

    /**
     * Constructor.
     */
    public ChangeDto(Change change, User user) {
        this.id = change.getId();
        this.uri = change.getUri();
        this.type = change.getChangeType();
        this.label = change.getLabel();
        this.subject = change.getSubject();
        this.predicate = change.getPredicate();
        this.object = change.getObject();
        this.newObject = change.getNewObject();
        this.approved = change.getApprovedBy().contains(user);
        this.rejected = change.getRejectedBy().contains(user);
    }

    @Override
    public int compareTo(ChangeDto o) {
        return subject.compareTo(o.subject);
    }
}
