package com.github.checkit.service.auxiliary;

import com.github.checkit.dao.ChangeDao;
import com.github.checkit.dao.ProjectContextDao;
import com.github.checkit.exception.UnexpectedRdfObjectException;
import com.github.checkit.exception.WrongChangeTypeException;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeObject;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.auxilary.AbstractChangeableContext;
import com.github.checkit.model.auxilary.ChangeSubjectType;
import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.OWL;
import cz.cvut.kbss.jopa.vocabulary.RDF;
import cz.cvut.kbss.jopa.vocabulary.SKOS;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class ChangeResolver {

    private final Model canonicalGraph;
    private final Model draftGraph;
    private final AbstractChangeableContext draftContext;
    private final ChangeDao changeDao;
    private final ProjectContextDao projectContextDao;
    private final ProjectContext project;

    private HashMap<Resource, Model> canonicalSubGraphs;
    private HashMap<Resource, Model> draftSubGraphs;
    private HashMap<Resource, List<Statement>> canonicalTopLevelSubjectsSubGraphs;
    private HashMap<Resource, List<Statement>> draftTopLevelSubjectsSubGraphs;
    @Getter
    private final List<Change> changes = new ArrayList<>();

    /**
     * Constructor.
     */
    public ChangeResolver(Model canonicalGraph, Model draftGraph, AbstractChangeableContext draftContext,
                          ProjectContext project, ProjectContextDao projectContextDao, ChangeDao changeDao) {
        this.canonicalGraph = canonicalGraph;
        this.draftGraph = draftGraph;
        this.draftContext = draftContext;
        this.project = project;
        this.projectContextDao = projectContextDao;
        this.changeDao = changeDao;
    }

    /**
     * Finds changes made in draft (modified vocabulary context) in statements that don't contain blank node comparison
     * to canonical version.
     */
    public void findChangesInStatementsWithoutBlankNode() {
        HashMap<Resource, ArrayList<Statement>> newStatements =
            getChangedStatementsWithoutBlankNodes(canonicalGraph, draftGraph);
        HashMap<Resource, ArrayList<Statement>> removedStatements =
            getChangedStatementsWithoutBlankNodes(draftGraph, canonicalGraph);

        for (Resource subject : removedStatements.keySet()) {
            MultilingualString label = fetchChangeLabel(subject, canonicalGraph);
            for (Statement statement : removedStatements.get(subject)) {
                ChangeType changeType = resolveChangeType(newStatements, statement);
                Change change = createChangeFromStatement(changeType, label,
                    fetchSubjectType(statement.getSubject(), canonicalGraph), statement);
                if (changeType == ChangeType.MODIFIED) {
                    Statement modifiedStatement = resolveModifiedStatement(statement, newStatements);
                    change.setNewObject(resolveObject(modifiedStatement.getObject()));
                    newStatements.get(subject).remove(modifiedStatement);
                }
                changes.add(change);
            }
        }

        for (Resource subject : newStatements.keySet()) {
            ArrayList<Statement> statements = newStatements.get(subject);
            if (statements.isEmpty()) {
                continue;
            }
            MultilingualString label = fetchChangeLabel(subject, draftGraph);
            for (Statement statement : statements) {
                Change change = createChangeFromStatement(ChangeType.CREATED, label,
                    fetchSubjectType(statement.getSubject(), draftGraph), statement);
                changes.add(change);
            }
        }
    }

    /**
     * Finds changes made in draft (modified vocabulary context) in statements that contain blank node in object
     * position and changes of these blank nodes in comparison to canonical version.
     */
    public void findChangesInSubGraphs() {
        canonicalSubGraphs = getSubGraphs(canonicalGraph);
        draftSubGraphs = getSubGraphs(draftGraph);
        canonicalTopLevelSubjectsSubGraphs = getTopLevelSubjectsSubGraphs(canonicalGraph);
        draftTopLevelSubjectsSubGraphs = getTopLevelSubjectsSubGraphs(draftGraph);
        removeIsomorphicSubGraphs();

        getChangesFromBlankNodeStatements();
    }

    private void removeIsomorphicSubGraphs() {
        //find isomorphic sub-graphs
        for (Resource subject : canonicalTopLevelSubjectsSubGraphs.keySet()) {
            if (!draftTopLevelSubjectsSubGraphs.containsKey(subject)) {
                continue;
            }
            List<Statement> canonicalSubjectSubGraphs = canonicalTopLevelSubjectsSubGraphs.get(subject);
            List<Statement> draftSubjectSubGraphs = draftTopLevelSubjectsSubGraphs.get(subject);
            List<Statement> canonicalSubjectStatementsToDelete = new ArrayList<>();
            List<Statement> draftSubjectStatementsToDelete = new ArrayList<>();

            for (Statement canonicalStatement : canonicalSubjectSubGraphs) {
                Resource canoicalResource = canonicalStatement.getObject().asResource();
                if (!canonicalSubGraphs.containsKey(canoicalResource)) {
                    continue;
                }
                Model canonicalSubGraph = canonicalSubGraphs.get(canoicalResource);
                for (Statement draftStatement : draftSubjectSubGraphs) {
                    Resource draftObject = draftStatement.getObject().asResource();
                    if (!draftSubjectStatementsToDelete.contains(draftStatement) && draftSubGraphs.containsKey(
                        draftObject) && canonicalSubGraph.isIsomorphicWith(draftSubGraphs.get(draftObject))) {
                        canonicalSubjectStatementsToDelete.add(canonicalStatement);
                        draftSubjectStatementsToDelete.add(draftStatement);
                        removeSubGraphRecursively(canonicalSubGraphs, canoicalResource);
                        removeSubGraphRecursively(draftSubGraphs, draftObject);
                        break;
                    }
                }
            }
            //remove isomorphic sub graphs
            canonicalSubjectSubGraphs.removeAll(canonicalSubjectStatementsToDelete);
            draftSubjectSubGraphs.removeAll(draftSubjectStatementsToDelete);
            canonicalTopLevelSubjectsSubGraphs.put(subject, canonicalSubjectSubGraphs);
            draftTopLevelSubjectsSubGraphs.put(subject, draftSubjectSubGraphs);
        }

        //remove subjects with no sub-graphs
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
    }

    private void removeSubGraphRecursively(HashMap<Resource, Model> subGraphs, Resource subGraphId) {
        Model removedSubGraph = subGraphs.remove(subGraphId);
        StmtIterator stmtIterator = removedSubGraph.listStatements();
        while (stmtIterator.hasNext()) {
            RDFNode object = stmtIterator.nextStatement().getObject();
            if (object.asNode().isBlank()) {
                removeSubGraphRecursively(subGraphs, object.asResource());
            }
        }
    }

    private void getChangesFromBlankNodeStatements() {
        for (Resource subject : draftTopLevelSubjectsSubGraphs.keySet()) {
            MultilingualString label = fetchChangeLabel(subject, draftGraph);
            for (Statement statement : draftTopLevelSubjectsSubGraphs.get(subject)) {
                Change change = createChangeFromStatement(ChangeType.CREATED, label,
                    fetchSubjectType(statement.getSubject(), draftGraph), statement);
                change.setUri(changeDao.generateEntityUri());
                changes.add(change);
                Model subGraph = draftSubGraphs.get(statement.getObject().asResource());
                findChangesOfSubGraph(subGraph, draftSubGraphs, change);
            }
        }
        for (Resource subject : canonicalTopLevelSubjectsSubGraphs.keySet()) {
            MultilingualString label = fetchChangeLabel(subject, canonicalGraph);
            for (Statement statement : canonicalTopLevelSubjectsSubGraphs.get(subject)) {
                Change change = createChangeFromStatement(ChangeType.REMOVED, label,
                    fetchSubjectType(statement.getSubject(), canonicalGraph), statement);
                change.setUri(changeDao.generateEntityUri());
                changes.add(change);
                Model subGraph = canonicalSubGraphs.get(statement.getObject().asResource());
                findChangesOfSubGraph(subGraph, canonicalSubGraphs, change);
            }
        }
    }

    private void findChangesOfSubGraph(Model subGraph, HashMap<Resource, Model> subGraphs, Change parentChange) {
        StmtIterator stmtIterator = subGraph.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            Change change = new Change(draftContext);
            change.setChangeType(parentChange.getChangeType());
            change.setSubjectType(ChangeSubjectType.BLANK_NODE);
            change.setLabel(parentChange.getLabel());
            change.setSubject(parentChange.getUri());
            change.setPredicate(URI.create(statement.getPredicate().getURI()));
            change.setObject(resolveObject(statement.getObject()));
            if (statement.getObject().asNode().isBlank()) {
                change.setUri(changeDao.generateEntityUri());
                changes.add(change);
                findChangesOfSubGraph(subGraphs.get(statement.getObject().asResource()), subGraphs, change);
            } else {
                changes.add(change);
            }
        }
    }

    private Statement resolveModifiedStatement(Statement oldStatement,
                                               HashMap<Resource, ArrayList<Statement>> newStatements) {
        Resource subject = oldStatement.getSubject();
        List<Statement> potentialStatements = newStatements.get(subject).stream()
            .filter(newStatement -> newStatement.getPredicate().equals(oldStatement.getPredicate())).toList();
        RDFNode oldObject = oldStatement.getObject();
        for (Statement potentialStatement : potentialStatements) {
            RDFNode newObject = potentialStatement.getObject();
            if (oldObject.isURIResource() && newObject.isURIResource()) {
                return potentialStatement;
            }
            if (oldObject.isLiteral() && newObject.isLiteral() && oldObject.asLiteral().getDatatype()
                .equals(newObject.asLiteral().getDatatype()) && oldObject.asLiteral().getLanguage()
                .equals(newObject.asLiteral().getLanguage())) {
                return potentialStatement;
            }
        }
        throw WrongChangeTypeException.create(oldStatement);
    }

    private ChangeType resolveChangeType(HashMap<Resource, ArrayList<Statement>> newSubjectsStatements,
                                         Statement oldStatement) {
        Resource subject = oldStatement.getSubject();
        if (!newSubjectsStatements.containsKey(subject)) {
            return ChangeType.REMOVED;
        }
        ArrayList<Statement> newStatements = newSubjectsStatements.get(subject);
        for (Statement newStatement : newStatements) {
            if (!newStatement.getPredicate().equals(oldStatement.getPredicate())) {
                continue;
            }
            RDFNode newObject = newStatement.getObject();
            RDFNode oldObject = oldStatement.getObject();
            if (oldObject.isURIResource()) {
                if (newObject.isURIResource()) {
                    return ChangeType.MODIFIED;
                }
                continue;
            }
            if (oldObject.isLiteral() && newObject.isLiteral() && oldObject.asLiteral().getDatatype()
                .equals(newObject.asLiteral().getDatatype()) && oldObject.asLiteral().getLanguage()
                .equals(newObject.asLiteral().getLanguage())) {
                return ChangeType.MODIFIED;
            }
        }
        return ChangeType.REMOVED;
    }

    private Change createChangeFromStatement(ChangeType changeType, MultilingualString label,
                                             ChangeSubjectType changeSubjectType, Statement statement) {

        Change change = new Change(draftContext);
        change.setChangeType(changeType);
        change.setSubjectType(changeSubjectType);
        change.setLabel(label);
        change.setSubject(URI.create(statement.getSubject().getURI()));
        change.setPredicate(URI.create(statement.getPredicate().getURI()));
        change.setObject(resolveObject(statement.getObject()));
        return change;
    }

    private ChangeObject resolveObject(RDFNode object) {
        if (object.isURIResource()) {
            return new ChangeObject(object.asResource().getURI(), null, null);
        }
        if (object.isLiteral()) {
            Literal literal = object.asLiteral();
            URI type = null;
            String language = null;
            if (!literal.getDatatype().equals(RDFLangString.rdfLangString)) {
                type = URI.create(literal.getDatatypeURI());
            }
            if (!literal.getLanguage().isEmpty()) {
                language = literal.getLanguage();
            }
            return new ChangeObject(literal.getString(), type, language);
        }
        if (object.asNode().isBlank()) {
            return new ChangeObject();
        }
        throw new UnexpectedRdfObjectException();
    }

    private MultilingualString fetchChangeLabel(Resource subject, Model graph) {
        MultilingualString multilingualString = new MultilingualString();
        StmtIterator labelStatementIterator = subject.listProperties(graph.getProperty(SKOS.PREF_LABEL));
        if (!labelStatementIterator.hasNext()) {
            labelStatementIterator = subject.listProperties(graph.getProperty(DC.Terms.TITLE));
        }
        if (labelStatementIterator.hasNext()) {
            do {
                Statement statement = labelStatementIterator.nextStatement();
                Literal literal = statement.getObject().asLiteral();
                multilingualString.set(literal.getLanguage(), literal.getString());
            } while (labelStatementIterator.hasNext());
        } else {
            multilingualString = projectContextDao.getPrefLabelOfIRI(project.getUri(), URI.create(subject.getURI()));
            if (multilingualString.isEmpty()) {
                multilingualString = projectContextDao.getPrefLabelOfIRI(URI.create(subject.getURI()));
            }
        }
        return multilingualString;
    }

    private ChangeSubjectType fetchSubjectType(Resource subject, Model graph) {
        Property type = graph.getProperty(RDF.TYPE);
        List<String> object =
            subject.listProperties(type).toList().stream().map(stm -> stm.getObject().asResource().getURI()).toList();
        if (object.contains(SKOS.CONCEPT)) {
            return ChangeSubjectType.TERM;
        }
        if (object.contains(OWL.ONTOLOGY)) {
            return ChangeSubjectType.VOCABULARY;
        }
        return ChangeSubjectType.UNKNOWN;
    }

    private HashMap<Resource, ArrayList<Statement>> getChangedStatementsWithoutBlankNodes(Model base, Model modified) {
        HashMap<Resource, ArrayList<Statement>> changedStatements = new HashMap<>();
        StmtIterator stmtIterator = modified.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            if (statement.asTriple().getSubject().isBlank() || statement.asTriple().getObject().isBlank()
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

    private HashMap<Resource, List<Statement>> getTopLevelSubjectsSubGraphs(Model model) {
        HashMap<Resource, List<Statement>> subjectsSubGraphs = new HashMap<>();
        StmtIterator stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            if (!statement.asTriple().getObject().isBlank() || statement.asTriple().getSubject().isBlank()) {
                continue;
            }
            if (!subjectsSubGraphs.containsKey(statement.getSubject())) {
                subjectsSubGraphs.put(statement.getSubject(), new ArrayList<>());
            }
            subjectsSubGraphs.get(statement.getSubject()).add(statement);
        }
        return subjectsSubGraphs;
    }
}
