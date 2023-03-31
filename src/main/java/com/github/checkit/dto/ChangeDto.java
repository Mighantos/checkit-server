package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.checkit.dto.auxiliary.ChangeState;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.User;
import java.net.URI;
import java.util.Comparator;
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
    private final ChangeState state;

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
        this.state = resolveChangeState(change, user);
    }

    private ChangeState resolveChangeState(Change change, User user) {
        if (change.getRejectedBy().contains(user)) {
            return ChangeState.REJECTED;
        }
        if (change.getApprovedBy().contains(user)) {
            return ChangeState.APPROVED;
        }
        return ChangeState.NOT_REVIEWED;
    }

    @Override
    public int compareTo(ChangeDto o) {
        return Comparator
            .comparing(ChangeDto::getSubject)
            .thenComparing(ChangeDto::getState)
            .thenComparing(ChangeDto::getType)
            .thenComparing(ChangeDto::getPredicate)
            .compare(this, o);
    }
}
