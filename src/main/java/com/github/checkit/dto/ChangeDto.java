package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.checkit.dto.auxiliary.ChangeState;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.User;
import com.github.checkit.model.auxilary.ChangeSubjectType;
import java.net.URI;
import java.util.Comparator;
import java.util.Objects;
import lombok.Getter;

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
    private final ChangeObjectDto object;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final ChangeObjectDto newObject;
    private final ChangeState state;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final CommentDto rejectionComment;
    @JsonIgnore
    private final boolean countable;

    /**
     * Constructor.
     */
    public ChangeDto(Change change, User user, String languageTag, String defaultLanguageTag,
                     CommentDto rejectionComment) {
        this.id = change.getId();
        this.uri = change.getUri();
        this.type = change.getChangeType();
        this.subjectType = change.getSubjectType();
        this.label = resolveLabel(change, languageTag, defaultLanguageTag);
        this.subject = change.getSubject();
        this.predicate = change.getPredicate();
        this.object = new ChangeObjectDto(change.getObject());
        if (Objects.nonNull(change.getNewObject())) {
            this.newObject = new ChangeObjectDto(change.getNewObject());
        } else {
            this.newObject = null;
        }
        this.state = resolveChangeState(change, user);
        this.rejectionComment = rejectionComment;
        this.countable = change.getCountable();
    }

    /**
     * Constructor for change before persisting.
     */
    public ChangeDto(Change change) {
        this.id = change.getId();
        this.uri = change.getUri();
        this.type = change.getChangeType();
        this.subjectType = change.getSubjectType();
        this.label = null;
        this.subject = change.getSubject();
        this.predicate = change.getPredicate();
        this.object = new ChangeObjectDto(change.getObject());
        if (Objects.nonNull(change.getNewObject())) {
            this.newObject = new ChangeObjectDto(change.getNewObject());
        } else {
            this.newObject = null;
        }
        this.state = null;
        this.rejectionComment = null;
        this.countable = true;
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
        this.object = new ChangeObjectDto(restrictionDto);
        this.newObject = null;
        this.state = changeState;
        this.rejectionComment = null;
        this.countable = false;
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
