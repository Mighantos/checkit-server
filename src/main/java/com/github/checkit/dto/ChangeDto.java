package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.checkit.dto.auxiliary.ChangeState;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeSubjectType;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.User;
import java.net.URI;
import java.util.Comparator;
import java.util.Objects;
import lombok.Getter;

@Getter
public class ChangeDto implements Comparable<ChangeDto> {

    private final String id;
    private final URI uri;
    private final ChangeType type;
    private final ChangeSubjectType subjectType;
    private final String label;
    private final URI subject;
    private final URI predicate;
    private final ObjectResourceDto object;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final ObjectResourceDto newObject;
    private final ChangeState state;

    /**
     * Constructor.
     */
    public ChangeDto(Change change, User user, String languageTag, String defaultLanguageTag) {
        this.id = change.getId();
        this.uri = change.getUri();
        this.type = change.getChangeType();
        this.subjectType = change.getSubjectType();
        this.label = resolveLabel(change, languageTag, defaultLanguageTag);
        this.subject = change.getSubject();
        this.predicate = change.getPredicate();
        this.object = new ObjectResourceDto(change.getObject());
        if (Objects.nonNull(change.getNewObject())) {
            this.newObject = new ObjectResourceDto(change.getNewObject());
        } else {
            this.newObject = null;
        }
        this.state = resolveChangeState(change, user);
    }

    private String resolveLabel(Change change, String languageTag, String defaultLanguageTag) {
        if (change.getLabel().contains(languageTag)) {
            return change.getLabel().get(languageTag);
        }
        if (change.getLabel().contains(defaultLanguageTag)) {
            return change.getLabel().get(defaultLanguageTag);
        }
        return change.getLabel().get();
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
            .comparing(ChangeDto::getSubjectType)
            .thenComparing(ChangeDto::getSubject)
            .thenComparing(ChangeDto::getState)
            .thenComparing(ChangeDto::getType)
            .thenComparing(ChangeDto::getPredicate)
            .compare(this, o);
    }
}
