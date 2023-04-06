package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.ChangeDao;
import com.github.checkit.exception.ForbiddenException;
import com.github.checkit.exception.WrongChangeTypeException;
import com.github.checkit.model.AbstractChangeableContext;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeSubjectType;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.ObjectResource;
import com.github.checkit.model.User;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.OWL;
import cz.cvut.kbss.jopa.vocabulary.RDF;
import cz.cvut.kbss.jopa.vocabulary.SKOS;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeService extends BaseRepositoryService<Change> {
    private final VocabularyService vocabularyService;
    private final VocabularyContextService vocabularyContextService;
    private final UserService userService;
    private final ChangeDao changeDao;

    /**
     * Constructor.
     */
    public ChangeService(VocabularyService vocabularyService, VocabularyContextService vocabularyContextService,
                         UserService userService, ChangeDao changeDao) {
        this.vocabularyService = vocabularyService;
        this.vocabularyContextService = vocabularyContextService;
        this.userService = userService;
        this.changeDao = changeDao;
    }

    @Override
    protected BaseDao<Change> getPrimaryDao() {
        return changeDao;
    }

    /**
     * Markes specified change as approved by current user.
     *
     * @param changeId identifier of change
     */
    @Transactional
    public void approveChange(String changeId) {
        User current = userService.getCurrent();
        URI changeUri = createChangeUriFromId(changeId);
        checkUserCanReviewChange(current.getUri(), changeUri);
        Change change = findRequired(changeUri);
        change.addApprovedBy(current);
        change.removeRejectedBy(current);
        changeDao.update(change);
    }

    /**
     * Markes specified change as approved by current user.
     *
     * @param changeId identifier of change
     */
    @Transactional
    public void rejectChange(String changeId) {
        User current = userService.getCurrent();
        URI changeUri = createChangeUriFromId(changeId);
        checkUserCanReviewChange(current.getUri(), changeUri);
        Change change = findRequired(changeUri);
        change.addRejectedBy(current);
        change.removeApprovedBy(current);
        changeDao.update(change);
    }

    /**
     * Returns list of changes made in specified vocabulary context compared to its canonical version.
     *
     * @param vocabularyContext {@link VocabularyContext} to find changes in
     * @return list of changes
     */
    public List<Change> getChanges(VocabularyContext vocabularyContext) {
        Model canonicalGraph =
            vocabularyService.getVocabularyContent(vocabularyContext.getBasedOnVocabulary().getUri());
        Model draftGraph = vocabularyContextService.getVocabularyContent(vocabularyContext.getUri());
        return getChanges(canonicalGraph, draftGraph, vocabularyContext);
    }

    /**
     * Returns list of changes made in specified draft compared to provided canonical version.
     *
     * @param canonicalGraph            Model representation of canonical graph (context)
     * @param draftGraph                Model representation of draft graph (context)
     * @param abstractChangeableContext context the changes were made in
     * @return list of changes
     */
    public List<Change> getChanges(Model canonicalGraph, Model draftGraph,
                                   AbstractChangeableContext abstractChangeableContext) {
        if (canonicalGraph.isIsomorphicWith(draftGraph)) {
            return new ArrayList<>();
        }
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

        HashMap<Resource, ArrayList<Statement>> newStatements =
            getChangedStatementsWithoutBlankNodes(canonicalGraph, draftGraph);
        HashMap<Resource, ArrayList<Statement>> removedStatements =
            getChangedStatementsWithoutBlankNodes(draftGraph, canonicalGraph);
        return getChangesFromStatements(abstractChangeableContext, newStatements, removedStatements, canonicalGraph,
            draftGraph);
    }

    private void checkUserCanReviewChange(URI userUri, URI changeUri) {
        if (!changeDao.isUserGestorOfVocabularyWithChange(userUri, changeUri)) {
            throw new ForbiddenException();
        }
    }

    private URI createChangeUriFromId(String id) {
        return URI.create(TermVocabulary.s_c_zmena + "/" + id);
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
            MultilingualString label = fetchChangeLabel(subject, draftGraph);
            for (Statement statement : newStatements.get(subject)) {
                Change change = new Change(abstractChangeableContext);
                change.setChangeType(ChangeType.CREATED);
                change.setSubjectType(fetchSubjectType(statement.getSubject(), draftGraph));
                change.setLabel(label);
                change.setSubject(URI.create(statement.getSubject().getURI()));
                change.setPredicate(URI.create(statement.getPredicate().getURI()));
                change.setObject(resolveObject(statement.getObject()));
                changes.add(change);
            }
        }

        for (Resource subject : removedStatements.keySet()) {
            MultilingualString label = fetchChangeLabel(subject, canonicalGraph);
            for (Statement statement : removedStatements.get(subject)) {
                ChangeType changeType = resolveChangeType(newStatements, statement);
                Change change = new Change(abstractChangeableContext);
                change.setChangeType(changeType);
                change.setSubjectType(fetchSubjectType(statement.getSubject(), draftGraph));
                change.setLabel(label);
                change.setSubject(URI.create(statement.getSubject().getURI()));
                change.setPredicate(URI.create(statement.getPredicate().getURI()));
                change.setObject(resolveObject(statement.getObject()));
                if (changeType == ChangeType.MODIFIED) {
                    Statement modifiedStatement = resolveModifiedStatement(statement, newStatements);
                    change.setNewObject(resolveObject(modifiedStatement.getObject()));
                }
                changes.add(change);
            }
        }
        return changes;
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
            if (oldObject.isLiteral() && newObject.isLiteral()
                && oldObject.asLiteral().getDatatype().equals(newObject.asLiteral().getDatatype())
                && oldObject.asLiteral().getLanguage().equals(newObject.asLiteral().getLanguage())) {
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
            if (oldObject.isLiteral() && newObject.isLiteral()
                && oldObject.asLiteral().getDatatype().equals(newObject.asLiteral().getDatatype())
                && oldObject.asLiteral().getLanguage().equals(newObject.asLiteral().getLanguage())) {
                return ChangeType.MODIFIED;
            }
        }
        return ChangeType.REMOVED;
    }

    private ObjectResource resolveObject(RDFNode object) {
        if (object.isURIResource()) {
            return new ObjectResource(object.asResource().getURI(), null, null);
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
            return new ObjectResource(literal.getString(), type, language);
        }
        throw new NotImplemented();
    }

    private MultilingualString fetchChangeLabel(Resource subject, Model graph) {
        MultilingualString multilingualString = new MultilingualString();
        StmtIterator labelStatementIterator =
            subject.listProperties(graph.getProperty(SKOS.PREF_LABEL));
        if (!labelStatementIterator.hasNext()) {
            labelStatementIterator = subject.listProperties(graph.getProperty(DC.Terms.TITLE));
        }
        while (labelStatementIterator.hasNext()) {
            Statement statement = labelStatementIterator.nextStatement();
            Literal literal = statement.getObject().asLiteral();
            multilingualString.set(literal.getLanguage(), literal.getString());
        }
        return multilingualString;
    }

    private ChangeSubjectType fetchSubjectType(Resource subject, Model graph) {
        Property type = graph.getProperty(RDF.TYPE);
        String object = graph.getProperty(subject, type).getObject().asResource().getURI();
        if (object.equals(SKOS.CONCEPT)) {
            return ChangeSubjectType.TERM;
        }
        if (object.equals(OWL.ONTOLOGY)) {
            return ChangeSubjectType.VOCABULARY;
        }
        return ChangeSubjectType.UNKNOWN;
    }

    private HashMap<Resource, ArrayList<Statement>> getChangedStatementsWithoutBlankNodes(Model base, Model modified) {
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
