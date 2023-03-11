package com.github.checkit.service;

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
public class ChangeService {
    private final VocabularyService vocabularyService;
    private final VocabularyContextService vocabularyContextService;

    public ChangeService(VocabularyService vocabularyService, VocabularyContextService vocabularyContextService) {
        this.vocabularyService = vocabularyService;
        this.vocabularyContextService = vocabularyContextService;
    }

    public String getChangesAsString(URI vocabularyContextUri) {
        VocabularyContext vc = vocabularyContextService.findRequired(vocabularyContextUri);
        Model changedVocabularyContent = vocabularyContextService.getVocabularyContent(vc.getUri());
        Model vocabularyContent = vocabularyService.getVocabularyContent(vc.getBasedOnVocabulary().getUri());
        return getChanges(vocabularyContent, changedVocabularyContent);
    }

    private String getChanges(Model canonicalGraph, Model draftGraph) {
        HashMap<Resource, ArrayList<Statement>> removedStatements = getChangedStatements(canonicalGraph, draftGraph);
        HashMap<Resource, ArrayList<Statement>> newStatements = getChangedStatements(draftGraph, canonicalGraph);
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

        String print = getChangesAsString(newStatements, removedStatements, canonicalGraph, draftGraph);
        System.out.println(print);
        return print;
    }

    private String getChangesAsString(HashMap<Resource, ArrayList<Statement>> newStatements,
                                      HashMap<Resource, ArrayList<Statement>> removedStatements,
                                      Model canonicalGraph, Model draftGraph) {
        String res = "\nadded:\n";
        for (Resource subject : newStatements.keySet()) {
            if (removedStatements.containsKey(subject)) {
                continue;
            }
            String text = "\n";
            Statement labelStatement =
                subject.getProperty(draftGraph.getProperty(SKOS.PREF_LABEL));
            if (labelStatement == null) {
                labelStatement = subject.getProperty(draftGraph.getProperty(DC.Terms.TITLE));
            }
            if (labelStatement != null) {
                text += labelStatement.getObject().asLiteral().getString();
            }
            text += " (" + subject + ")";
            for (Statement statement : newStatements.get(subject)) {
                text += "\n+ " + statement.toString();
            }
            res += text + "\n";
        }

        res += "\nchanged:\n";
        for (Resource subject : removedStatements.keySet()) {
            if (!newStatements.containsKey(subject)) {
                continue;
            }
            String text = "";
            Statement labelStatement =
                subject.getProperty(canonicalGraph.getProperty(SKOS.PREF_LABEL));
            if (labelStatement == null) {
                labelStatement = subject.getProperty(canonicalGraph.getProperty(DC.Terms.TITLE));
            }
            if (labelStatement != null) {
                text += labelStatement.getObject().asLiteral().getString();
            }
            text += " (" + subject + ")";
            text += "\n- " + removedStatements.get(subject);
            text += "\n+ ";
            text += newStatements.get(subject).toString();
            res += text + "\n";
        }

        res += "\nremoved:\n";
        for (Resource subject : removedStatements.keySet()) {
            if (newStatements.containsKey(subject)) {
                continue;
            }
            String text = "";
            Statement labelStatement =
                subject.getProperty(canonicalGraph.getProperty(SKOS.PREF_LABEL));
            if (labelStatement == null) {
                labelStatement = subject.getProperty(canonicalGraph.getProperty(DC.Terms.TITLE));
            }
            if (labelStatement != null) {
                text += labelStatement.getObject().asLiteral().getString();
            }
            text += " (" + subject + ")";
            text += "\n- " + removedStatements.get(subject);
            res += text + "\n";
        }
        return res;
    }

    private HashMap<Resource, ArrayList<Statement>> getChangedStatements(Model base, Model modified) {
        HashMap<Resource, ArrayList<Statement>> changedStatements = new HashMap<>();
        StmtIterator stmtIterator = base.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            if (statement.asTriple().getSubject().isBlank()
                || statement.asTriple().getObject().isBlank()
                || modified.contains(statement)) {
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
