package com.github.checkit.service.auxiliary;

import com.github.checkit.dto.ChangeDto;
import com.github.checkit.dto.RestrictionDto;
import com.github.checkit.dto.auxiliary.ChangeState;
import com.github.checkit.model.ChangeSubjectType;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.vocabulary.RDF;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.eclipse.rdf4j.model.vocabulary.OWL;

public class ChangeDtoComposer {

    private final List<ChangeDto> changeDtos;
    @Getter
    private final List<ChangeDto> groupChangeDtosOfRestrictions = new ArrayList<>();
    private final Map<URI, List<ChangeDto>> subjectsChangeDtosPointingAtBlankNodeMap = new HashMap<>();
    private List<ChangeDto> changeDtosInBlankNodes;

    public ChangeDtoComposer(List<ChangeDto> changeDtos) {
        this.changeDtos = changeDtos;
    }

    /**
     * Composes change DTOs to contain restrictions and hides changes composed in restrictions.
     */
    public void compose() {
        List<ChangeDto> changesPointingAtBlankNode = changeDtos.stream().filter(changeDto ->
                !changeDto.getSubjectType().equals(ChangeSubjectType.BLANK_NODE) && changeDto.getObject().isBlankNode())
            .toList();
        changeDtosInBlankNodes =
            changeDtos.stream().filter(change -> change.getSubjectType().equals(ChangeSubjectType.BLANK_NODE)).toList();
        for (ChangeDto changePointingAtBlankNode : changesPointingAtBlankNode) {
            URI subject = changePointingAtBlankNode.getSubject();
            if (!subjectsChangeDtosPointingAtBlankNodeMap.containsKey(subject)) {
                subjectsChangeDtosPointingAtBlankNodeMap.put(subject, new ArrayList<>());
            }
            subjectsChangeDtosPointingAtBlankNodeMap.get(subject).add(changePointingAtBlankNode);
        }
        createGroupChangeDtosOfRestriction();
    }

    private void createGroupChangeDtosOfRestriction() {
        for (URI subject : subjectsChangeDtosPointingAtBlankNodeMap.keySet()) {
            List<ChangeDto> subjectChangeDtosPointingAtBlankNode =
                subjectsChangeDtosPointingAtBlankNodeMap.get(subject);
            RestrictionDto restrictionDto = createRestrictionFromChanges(subjectChangeDtosPointingAtBlankNode);
            if (Objects.isNull(restrictionDto)) {
                continue;
            }
            changeDtos.removeAll(restrictionDto.getChanges());
            ChangeDto changeDto = new ChangeDto(subjectChangeDtosPointingAtBlankNode.get(0), restrictionDto,
                resolveReviewState(restrictionDto));
            groupChangeDtosOfRestrictions.add(changeDto);
        }
    }

    private RestrictionDto createRestrictionFromChanges(List<ChangeDto> subjectChangeDtosPointingAtBlankNode) {
        RestrictionDto restrictionDto = new RestrictionDto();
        List<ChangeDto> affectedChanges = new ArrayList<>();
        for (ChangeDto topLevelChangeDto : subjectChangeDtosPointingAtBlankNode) {
            affectedChanges.add(topLevelChangeDto);
            List<ChangeDto> blankNodeChanges = changeDtosInBlankNodes.stream()
                .filter(changeDto -> changeDto.getSubject().equals(topLevelChangeDto.getUri())).toList();
            if (!isChangeOnRelation(blankNodeChanges) || !isRestriction(blankNodeChanges)) {
                return null;
            }
            if (Objects.isNull(restrictionDto.getRelationUri())) {
                restrictionDto.setRelationUri(topLevelChangeDto.getSubject());
                restrictionDto.setRelationName(topLevelChangeDto.getLabel());
            }
            boolean start = isStart(blankNodeChanges);
            for (ChangeDto blankNodeChange : blankNodeChanges) {
                affectedChanges.add(blankNodeChange);
                String predicate = blankNodeChange.getPredicate().toString();
                if (predicate.equals(OWL.MINQUALIFIEDCARDINALITY.toString())) {
                    int minCardinality = Integer.parseInt(blankNodeChange.getObject().getValue());
                    if (start) {
                        if (Objects.nonNull(restrictionDto.getCardinalityStart().getMin())) {
                            continue;
                        }
                        restrictionDto.getCardinalityStart().setMin(minCardinality);
                    } else {
                        if (Objects.nonNull(restrictionDto.getCardinalityEnd().getMin())) {
                            continue;
                        }
                        restrictionDto.getCardinalityEnd().setMin(minCardinality);
                    }
                } else if (predicate.equals(OWL.MAXQUALIFIEDCARDINALITY.toString())) {
                    int maxCardinality = Integer.parseInt(blankNodeChange.getObject().getValue());
                    if (start) {
                        if (Objects.nonNull(restrictionDto.getCardinalityStart().getMax())) {
                            continue;
                        }
                        restrictionDto.getCardinalityStart().setMax(maxCardinality);
                    } else {
                        if (Objects.nonNull(restrictionDto.getCardinalityEnd().getMax())) {
                            continue;
                        }
                        restrictionDto.getCardinalityEnd().setMax(maxCardinality);
                    }
                } else if (predicate.equals(OWL.ONCLASS.toString())) {
                    URI onClass = URI.create(blankNodeChange.getObject().getValue());
                    if (start) {
                        if (Objects.nonNull(restrictionDto.getStartUri())) {
                            continue;
                        }
                        restrictionDto.setStartUri(onClass);
                        restrictionDto.setStartName(resolveLabelOfAffectedClass(onClass));
                    } else {
                        if (Objects.nonNull(restrictionDto.getEndUri())) {
                            continue;
                        }
                        restrictionDto.setEndUri(onClass);
                        restrictionDto.setEndName(resolveLabelOfAffectedClass(onClass));
                    }
                    affectedChanges.addAll(traverseAffectedClass(onClass, restrictionDto.getRelationUri()));
                }
            }
        }
        restrictionDto.setChanges(affectedChanges);
        return restrictionDto;
    }

    private List<ChangeDto> traverseAffectedClass(URI onClass, URI relationUri) {
        assert !onClass.equals(relationUri);
        List<ChangeDto> subjectChangeDtosPointingAtBlankNode = subjectsChangeDtosPointingAtBlankNodeMap.get(onClass);
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

    private ChangeState resolveReviewState(RestrictionDto restrictionDto) {
        List<ChangeDto> affectedChanges = restrictionDto.getChanges();
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

    private boolean isChangeOnRelation(List<ChangeDto> blankNodeChanges) {
        return blankNodeChanges.stream().anyMatch(changeDto ->
            changeDto.getPredicate().equals(URI.create(OWL.ONPROPERTY.toString()))
                && !changeDto.getObject().isBlankNode());
    }
}
