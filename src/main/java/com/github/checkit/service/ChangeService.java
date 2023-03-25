package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.ChangeDao;
import com.github.checkit.model.AbstractChangeableContext;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.VocabularyContext;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.SKOS;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.springframework.stereotype.Service;

@Service
public class ChangeService extends BaseRepositoryService<Change> {
    private final VocabularyService vocabularyService;
    private final VocabularyContextService vocabularyContextService;
    private final ChangeDao changeDao;

    /**
     * Constructor.
     */
    public ChangeService(VocabularyService vocabularyService, VocabularyContextService vocabularyContextService,
                         ChangeDao changeDao) {
        this.vocabularyService = vocabularyService;
        this.vocabularyContextService = vocabularyContextService;
        this.changeDao = changeDao;
    }

    @Override
    protected BaseDao<Change> getPrimaryDao() {
        return changeDao;
    }

    public List<Change> getChanges(VocabularyContext vocabularyContext) {
        Model canonicalGraph =
            vocabularyService.getVocabularyContent(vocabularyContext.getBasedOnVocabulary().getUri());
        Model draftGraph = vocabularyContextService.getVocabularyContent(vocabularyContext.getUri());
        return getChanges(canonicalGraph, draftGraph, vocabularyContext);
    }

    public List<Change> getChanges(Model canonicalGraph, Model draftGraph,
                                   AbstractChangeableContext abstractChangeableContext) {
        HashMap<Resource, ArrayList<Statement>> newStatements = getChangedStatements(canonicalGraph, draftGraph);
        HashMap<Resource, ArrayList<Statement>> removedStatements = getChangedStatements(draftGraph, canonicalGraph);
        HashMap<Resource, Model> canonicalSubGraphs = getSubGraphs(canonicalGraph);
        HashMap<Resource, Model> draftSubGraphs = getSubGraphs(draftGraph);
        HashMap<Resource, ArrayList<Resource>> canonicalTopLevelSubjectsSubGraphs =
            getTopLevelSubjectsSubGraphs(canonicalGraph);
        HashMap<Resource, ArrayList<Resource>> draftTopLevelSubjectsSubGraphs =
            getTopLevelSubjectsSubGraphs(draftGraph);

        //remove isomorphic subGraphs
        for (Resource subject : canonicalTopLevelSubjectsSubGraphs.keySet()) {
            if (!draftTopLevelSubjectsSubGraphs.containsKey(subject)) {
                continue;
            }
            ArrayList<Resource> canonicalResources = canonicalTopLevelSubjectsSubGraphs.get(subject);
            ArrayList<Resource> draftResources = draftTopLevelSubjectsSubGraphs.get(subject);
            ArrayList<Resource> canonicalResourcesToDelete = new ArrayList<>();
            ArrayList<Resource> draftResourcesToDelete = new ArrayList<>();
            for (Resource canoicalResource : canonicalResources) {
                if (!canonicalSubGraphs.containsKey(canoicalResource)) {
                    continue;
                }
                Model model = canonicalSubGraphs.get(canoicalResource);
                for (Resource draftResource : draftResources) {
                    if (!draftResourcesToDelete.contains(draftResource)
                        && draftSubGraphs.containsKey(draftResource)
                        && model.isIsomorphicWith(draftSubGraphs.get(draftResource))
                    ) {
                        canonicalResourcesToDelete.add(canoicalResource);
                        draftResourcesToDelete.add(draftResource);
                        canonicalSubGraphs.remove(canoicalResource);
                        draftSubGraphs.remove(draftResource);
                        break;
                    }
                }
            }
            canonicalResources.removeAll(canonicalResourcesToDelete);
            draftResources.removeAll(draftResourcesToDelete);
            canonicalTopLevelSubjectsSubGraphs.put(subject, canonicalResources);
            draftTopLevelSubjectsSubGraphs.put(subject, draftResources);
        }
        List<Resource> subjectsToRemove = canonicalTopLevelSubjectsSubGraphs.keySet().stream()
            .filter(s -> canonicalTopLevelSubjectsSubGraphs.get(s).isEmpty()).toList();
        for (Resource subject : subjectsToRemove) {
            canonicalTopLevelSubjectsSubGraphs.remove(subject);
        }
        subjectsToRemove = draftTopLevelSubjectsSubGraphs.keySet().stream()
            .filter(s -> draftTopLevelSubjectsSubGraphs.get(s).isEmpty()).toList();
        for (Resource subject : subjectsToRemove) {
            draftTopLevelSubjectsSubGraphs.remove(subject);
        }

        return getChangesFromStatements(abstractChangeableContext, newStatements, removedStatements, canonicalGraph,
            draftGraph);
    }

    private List<Change> getChangesFromStatements(AbstractChangeableContext abstractChangeableContext,
                                                  HashMap<Resource, ArrayList<Statement>> newStatements,
                                                  HashMap<Resource, ArrayList<Statement>> removedStatements,
                                                  Model canonicalGraph, Model draftGraph) {
        List<Change> changes = new ArrayList<>();
        for (Resource subject : newStatements.keySet()) {
            if (removedStatements.containsKey(subject)) {
                continue;
            }
            String label = fetchChangeLabel(subject, draftGraph);
            for (Statement statement : newStatements.get(subject)) {
                Change change = new Change(abstractChangeableContext);
                change.setChangeType(ChangeType.CREATED);
                change.setLabel(label);
                change.setSubject(URI.create(statement.getSubject().getURI()));
                change.setPredicate(URI.create(statement.getPredicate().getURI()));
                change.setObject(statement.getObject().toString());
                changes.add(change);
            }
        }

        for (Resource subject : removedStatements.keySet()) {
            ChangeType changeType;
            if (newStatements.containsKey(subject)) {
                changeType = ChangeType.MODIFIED;
            } else {
                changeType = ChangeType.REMOVED;
            }
            String label = fetchChangeLabel(subject, canonicalGraph);
            for (Statement statement : removedStatements.get(subject)) {
                Change change = new Change(abstractChangeableContext);
                change.setChangeType(changeType);
                change.setLabel(label);
                change.setSubject(URI.create(statement.getSubject().getURI()));
                change.setPredicate(URI.create(statement.getPredicate().getURI()));
                change.setObject(statement.getObject().toString());
                if (changeType == ChangeType.MODIFIED) {
                    Statement modifiedStatement =
                        draftGraph.getProperty(statement.getSubject(), statement.getPredicate());
                    change.setNewObject(modifiedStatement.getObject().toString());
                }
                changes.add(change);
            }
        }
        return changes;
    }

    private String fetchChangeLabel(Resource subject, Model graph) {
        Statement labelStatement =
            subject.getProperty(graph.getProperty(SKOS.PREF_LABEL));
        String label = null;
        if (labelStatement == null) {
            labelStatement = subject.getProperty(graph.getProperty(DC.Terms.TITLE));
        }
        if (labelStatement != null) {
            label = labelStatement.getObject().asLiteral().getString();
        }
        return label;
    }

    private HashMap<Resource, ArrayList<Statement>> getChangedStatements(Model base, Model modified) {
        HashMap<Resource, ArrayList<Statement>> changedStatements = new HashMap<>();
        StmtIterator stmtIterator = modified.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            if (statement.asTriple().getSubject().isBlank()
                || statement.asTriple().getObject().isBlank()
                || base.contains(statement)) {
                continue;
            }
            if (!changedStatements.containsKey(statement.getSubject())) {
                changedStatements.put(statement.getSubject(), new ArrayList<>());
            }
            changedStatements.get(statement.getSubject()).add(statement);
        }
        return changedStatements;
    }

    private HashMap<Resource, Model> getSubGraphs(Model model) {
        HashMap<Resource, Model> subGraphs = new HashMap<>();
        StmtIterator stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            if (!statement.asTriple().getSubject().isBlank()) {
                continue;
            }
            if (!subGraphs.containsKey(statement.getSubject())) {
                subGraphs.put(statement.getSubject(), ModelFactory.createDefaultModel());
            }
            subGraphs.get(statement.getSubject()).add(statement);
        }
        return subGraphs;
    }

    private HashMap<Resource, ArrayList<Resource>> getTopLevelSubjectsSubGraphs(Model model) {
        HashMap<Resource, ArrayList<Resource>> subjectsSubGraphs = new HashMap<>();
        StmtIterator stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            if (!statement.asTriple().getObject().isBlank() || statement.asTriple().getSubject().isBlank()) {
                continue;
            }
            if (!subjectsSubGraphs.containsKey(statement.getSubject())) {
                subjectsSubGraphs.put(statement.getSubject(), new ArrayList<>());
            }
            subjectsSubGraphs.get(statement.getSubject())
                .add(statement.getObject().asResource());
        }
        return subjectsSubGraphs;
    }
}
