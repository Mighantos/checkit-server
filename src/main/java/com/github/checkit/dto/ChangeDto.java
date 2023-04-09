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
import lombok.Setter;

@Getter
public class ChangeDto implements Comparable<ChangeDto> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
    private boolean visible;

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
        this.visible = true;
    }

    /**
     * Constructor for change with restriction.
     */
    public ChangeDto(ChangeDto changeDto, RestrictionDto restrictionDto, ChangeState changeState) {
        this.id = null;
        this.uri = null;
        this.type = changeDto.getType();
        this.subjectType = changeDto.getSubjectType();
        this.label = changeDto.getLabel();
        this.subject = changeDto.getSubject();
        this.predicate = changeDto.getPredicate();
        this.object = new ObjectResourceDto(restrictionDto);
        this.newObject = null;
        this.state = changeState;
        this.visible = true;
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

    public void markNotVisible() {
        this.visible = false;
    }

    private boolean isHidden() {
        return !isVisible();
    }

    @Override
    public int compareTo(ChangeDto o) {
        return Comparator
            .comparing(ChangeDto::isHidden)
            .thenComparing(ChangeDto::getSubjectType)
            .thenComparing(ChangeDto::getSubject)
            .thenComparing(ChangeDto::getState)
            .thenComparing(ChangeDto::getType)
            .thenComparing(ChangeDto::getPredicate)
            .compare(this, o);
    }
}
