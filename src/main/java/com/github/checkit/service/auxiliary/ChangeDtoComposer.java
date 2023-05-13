package com.github.checkit.service.auxiliary;

import com.github.checkit.dto.ChangeDto;
import com.github.checkit.dto.RelationshipDto;
import com.github.checkit.dto.auxiliary.ChangeState;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.auxilary.ChangeSubjectType;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.vocabulary.RDF;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import org.eclipse.rdf4j.model.vocabulary.OWL;

public class ChangeDtoComposer {

    private final List<ChangeDto> changeDtos;
    @Getter
    private final List<ChangeDto> groupChangeDtosOfRelationships = new ArrayList<>();
    private Map<URI, List<ChangeDto>> subjectsChangeDtosPointingAtBlankNodeMap;
    private List<ChangeDto> changeDtosInBlankNodes;

    public ChangeDtoComposer(List<ChangeDto> changeDtos) {
        this.changeDtos = changeDtos;
    }

    /**
     * Composes change DTOs to contain relationship and hides changes composed in restrictions.
     */
    public void compose() {
        Map<URI, List<ChangeDto>> subjectsCreateChangeDtosPointingAtBlankNodeMap = new HashMap<>();
        Map<URI, List<ChangeDto>> subjectsDeleteChangeDtosPointingAtBlankNodeMap = new HashMap<>();
        List<ChangeDto> changesPointingAtBlankNode = changeDtos.stream().filter(changeDto ->
                !changeDto.getSubjectType().equals(ChangeSubjectType.BLANK_NODE) && changeDto.getObject().isBlankNode())
            .toList();
        changeDtosInBlankNodes =
            changeDtos.stream().filter(change -> change.getSubjectType().equals(ChangeSubjectType.BLANK_NODE)).toList();
        for (ChangeDto changePointingAtBlankNode : changesPointingAtBlankNode) {
            URI subject = changePointingAtBlankNode.getSubject();
            if (changePointingAtBlankNode.getType().equals(ChangeType.CREATED)) {
                if (!subjectsCreateChangeDtosPointingAtBlankNodeMap.containsKey(subject)) {
                    subjectsCreateChangeDtosPointingAtBlankNodeMap.put(subject, new ArrayList<>());
                }
                subjectsCreateChangeDtosPointingAtBlankNodeMap.get(subject).add(changePointingAtBlankNode);
            } else {
                if (!subjectsDeleteChangeDtosPointingAtBlankNodeMap.containsKey(subject)) {
                    subjectsDeleteChangeDtosPointingAtBlankNodeMap.put(subject, new ArrayList<>());
                }
                subjectsDeleteChangeDtosPointingAtBlankNodeMap.get(subject).add(changePointingAtBlankNode);
            }
        }
        subjectsChangeDtosPointingAtBlankNodeMap = subjectsCreateChangeDtosPointingAtBlankNodeMap;
        createGroupChangeDtosOfRelationships();
        subjectsChangeDtosPointingAtBlankNodeMap = subjectsDeleteChangeDtosPointingAtBlankNodeMap;
        createGroupChangeDtosOfRelationships();
        selectCommentableChangeInGroups();
    }

    /**
     * Gets a set of Changes that are countable to statistics.
     *
     * @return set of URIs of changes
     */
    public Set<URI> getCountable() {
        Set<URI> countableChanges = new HashSet<>(changeDtos.stream().map(ChangeDto::getUri).toList());
        countableChanges.addAll(groupChangeDtosOfRelationships.stream()
            .map(changeDto -> changeDto.getObject().getRestriction().getCommentableChange()).toList());
        return countableChanges;
    }

    private void selectCommentableChangeInGroups() {
        for (ChangeDto groupChangeDtosOfRelationship : groupChangeDtosOfRelationships) {
            RelationshipDto relationship = groupChangeDtosOfRelationship.getObject().getRestriction();
            List<ChangeDto> affectedChanges = relationship.getAffectedChanges();
            ChangeDto commentableChangeDto = affectedChanges.stream()
                .filter(ChangeDto::isCountable).findFirst()
                .orElse(affectedChanges.iterator().next());
            relationship.setCommentableChange(commentableChangeDto.getUri());
        }
    }

    private void createGroupChangeDtosOfRelationships() {
        for (URI subject : subjectsChangeDtosPointingAtBlankNodeMap.keySet()) {
            List<ChangeDto> subjectChangeDtosPointingAtBlankNode =
                subjectsChangeDtosPointingAtBlankNodeMap.get(subject);
            if (subjectChangeDtosPointingAtBlankNode.isEmpty()) {
                continue;
            }
            RelationshipDto relationshipDto = createRelationshipFromChanges(subjectChangeDtosPointingAtBlankNode);
            if (Objects.isNull(relationshipDto)) {
                continue;
            }
            changeDtos.removeAll(relationshipDto.getAffectedChanges());
            ChangeDto changeDto = new ChangeDto(subjectChangeDtosPointingAtBlankNode.get(0), relationshipDto,
                resolveReviewState(relationshipDto));
            groupChangeDtosOfRelationships.add(changeDto);
        }
    }

    private RelationshipDto createRelationshipFromChanges(List<ChangeDto> subjectChangeDtosPointingAtBlankNode) {
        RelationshipDto relationshipDto = new RelationshipDto();
        List<ChangeDto> affectedChanges = new ArrayList<>();
        try {
            for (ChangeDto topLevelChangeDto : subjectChangeDtosPointingAtBlankNode) {
                affectedChanges.add(topLevelChangeDto);
                List<ChangeDto> blankNodeChanges = changeDtosInBlankNodes.stream()
                    .filter(changeDto -> changeDto.getSubject().equals(topLevelChangeDto.getUri())).toList();
                if (!isChangeOnRelationship(blankNodeChanges) || !isRestriction(blankNodeChanges)) {
                    return null;
                }
                if (Objects.isNull(relationshipDto.getRelationUri())) {
                    relationshipDto.setRelationUri(topLevelChangeDto.getSubject());
                    relationshipDto.setRelationName(topLevelChangeDto.getLabel());
                }
                boolean start = isStart(blankNodeChanges);
                for (ChangeDto blankNodeChange : blankNodeChanges) {
                    affectedChanges.add(blankNodeChange);
                    String predicate = blankNodeChange.getPredicate().toString();
                    if (predicate.equals(OWL.MINQUALIFIEDCARDINALITY.toString())) {
                        int minCardinality = Integer.parseInt(blankNodeChange.getObject().getValue());
                        if (start) {
                            if (Objects.nonNull(relationshipDto.getCardinalityStart().getMin())) {
                                continue;
                            }
                            relationshipDto.getCardinalityStart().setMin(minCardinality);
                        } else {
                            if (Objects.nonNull(relationshipDto.getCardinalityEnd().getMin())) {
                                continue;
                            }
                            relationshipDto.getCardinalityEnd().setMin(minCardinality);
                        }
                    } else if (predicate.equals(OWL.MAXQUALIFIEDCARDINALITY.toString())) {
                        int maxCardinality = Integer.parseInt(blankNodeChange.getObject().getValue());
                        if (start) {
                            if (Objects.nonNull(relationshipDto.getCardinalityStart().getMax())) {
                                continue;
                            }
                            relationshipDto.getCardinalityStart().setMax(maxCardinality);
                        } else {
                            if (Objects.nonNull(relationshipDto.getCardinalityEnd().getMax())) {
                                continue;
                            }
                            relationshipDto.getCardinalityEnd().setMax(maxCardinality);
                        }
                    } else if (predicate.equals(OWL.ONCLASS.toString())) {
                        URI onClass = URI.create(blankNodeChange.getObject().getValue());
                        if (start) {
                            if (Objects.nonNull(relationshipDto.getStartUri())) {
                                continue;
                            }
                            relationshipDto.setStartUri(onClass);
                            relationshipDto.setStartName(resolveLabelOfAffectedClass(onClass));
                        } else {
                            if (Objects.nonNull(relationshipDto.getEndUri())) {
                                continue;
                            }
                            relationshipDto.setEndUri(onClass);
                            relationshipDto.setEndName(resolveLabelOfAffectedClass(onClass));
                        }
                        affectedChanges.addAll(traverseAffectedClass(onClass, relationshipDto.getRelationUri()));
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        relationshipDto.setAffectedChanges(affectedChanges);
        return relationshipDto;
    }

    private List<ChangeDto> traverseAffectedClass(URI onClass, URI relationUri) {
        assert !onClass.equals(relationUri);
        List<ChangeDto> subjectChangeDtosPointingAtBlankNode =
            subjectsChangeDtosPointingAtBlankNodeMap.get(onClass);
        List<ChangeDto> affectedChanges = new ArrayList<>();
        List<ChangeDto> affectedTopLevelChangeDtos = new ArrayList<>();
        for (ChangeDto topLevelChangeDto : subjectChangeDtosPointingAtBlankNode) {
            List<ChangeDto> blankNodeChanges = changeDtosInBlankNodes.stream()
                .filter(changeDto -> changeDto.getSubject().equals(topLevelChangeDto.getUri())).toList();
            if (!isAboutRelation(blankNodeChanges, relationUri)) {
                continue;
            }
            affectedTopLevelChangeDtos.add(topLevelChangeDto);
            affectedChanges.add(topLevelChangeDto);
            affectedChanges.addAll(recursiveTraverseOfAffectedChanges(blankNodeChanges));
        }
        subjectsChangeDtosPointingAtBlankNodeMap.get(onClass).removeAll(affectedTopLevelChangeDtos);
        return affectedChanges;
    }

    private List<ChangeDto> recursiveTraverseOfAffectedChanges(List<ChangeDto> blankNodeChanges) {
        List<ChangeDto> affectedChanges = new ArrayList<>();
        for (ChangeDto blankNodeChange : blankNodeChanges) {
            affectedChanges.add(blankNodeChange);
            if (blankNodeChange.getObject().isBlankNode()) {
                affectedChanges.addAll(recursiveTraverseOfAffectedChanges(changeDtosInBlankNodes.stream()
                    .filter(changeDto -> changeDto.getSubject().equals(blankNodeChange.getUri())).toList()));
            }
        }
        return affectedChanges;
    }

    private ChangeState resolveReviewState(RelationshipDto relationshipDto) {
        List<ChangeDto> affectedChanges = relationshipDto.getAffectedChanges();
        boolean potentiallyApproved = true;
        boolean potentiallyRejected = true;
        for (ChangeDto changeDto : affectedChanges) {
            if ((!potentiallyApproved && !potentiallyRejected)
                || changeDto.getState().equals(ChangeState.NOT_REVIEWED)) {
                return ChangeState.NOT_REVIEWED;
            }
            if (changeDto.getState().equals(ChangeState.APPROVED)) {
                potentiallyRejected = false;
            } else if (changeDto.getState().equals(ChangeState.REJECTED)) {
                potentiallyApproved = false;
            }
        }
        if (potentiallyApproved) {
            return ChangeState.APPROVED;
        }
        if (potentiallyRejected) {
            return ChangeState.REJECTED;
        }
        return ChangeState.NOT_REVIEWED;
    }

    private String resolveLabelOfAffectedClass(URI onClass) {
        ChangeDto changeDto = subjectsChangeDtosPointingAtBlankNodeMap.get(onClass).get(0);
        return changeDto.getLabel();
    }

    private boolean isAboutRelation(List<ChangeDto> blankNodeChanges, URI relationUri) {
        return blankNodeChanges.stream().anyMatch(changeDto ->
            (changeDto.getPredicate().equals(URI.create(OWL.ONCLASS.toString()))
                || changeDto.getPredicate().equals(URI.create(OWL.SOMEVALUESFROM.toString()))
                || changeDto.getPredicate().equals(URI.create(OWL.ALLVALUESFROM.toString()))
            ) && changeDto.getObject().getValue().equals(relationUri.toString()));
    }

    private boolean isStart(List<ChangeDto> blankNodeChanges) {
        return blankNodeChanges.stream().anyMatch(changeDto ->
            changeDto.getPredicate().equals(URI.create(OWL.ONPROPERTY.toString()))
                && changeDto.getObject().getValue().equals(TermVocabulary.s_p_ma_vztazeny_prvek_1));
    }

    private boolean isRestriction(List<ChangeDto> blankNodeChanges) {
        return blankNodeChanges.stream().anyMatch(changeDto ->
            changeDto.getPredicate().equals(URI.create(RDF.TYPE))
                && changeDto.getObject().getValue().equals(OWL.RESTRICTION.toString()));
    }

    private boolean isChangeOnRelationship(List<ChangeDto> blankNodeChanges) {
        return blankNodeChanges.stream().anyMatch(changeDto ->
            changeDto.getPredicate().equals(URI.create(OWL.ONPROPERTY.toString()))
                && !changeDto.getObject().isBlankNode());
    }
}
