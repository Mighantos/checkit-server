package com.github.checkit.service;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.dto.PublicationContextDto;
import com.github.checkit.environment.Generator;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeObject;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.persistence.DescriptorFactory;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PublicationContextServiceTest extends BaseServiceTestRunner {

    @Autowired
    private EntityManager em;

    @Autowired
    private DescriptorFactory descriptorFactory;

    @Autowired
    private PublicationContextService sut;

    @Autowired
    private RepositoryConfigProperties repositoryConfigProperties;

    private User user;
    private ProjectContext projectContext;
    private VocabularyContext vocabularyContext;
    private Vocabulary vocabulary;
    private PublicationContext publicationContext;

    @BeforeEach
    void setUp() {
        this.user = Generator.generateDefaultUser(repositoryConfigProperties.getUser().getIdPrefix());
        transactional(() -> em.persist(user));

        this.vocabulary = Generator.generateVocabularyWithUri();
        this.vocabularyContext = Generator.generateVocabularyContextWithUri();
        this.vocabularyContext.setBasedOnVocabulary(vocabulary);
        this.projectContext = Generator.generateProjectContextWithUri();
        this.projectContext.setVocabularyContexts(Collections.singleton(vocabularyContext));
        this.publicationContext = Generator.generatePublicationContextWithUri();
        this.publicationContext.setFromProject(projectContext);
        this.publicationContext.setChanges(
            Collections.singleton(Generator.generateCreateChangeWitUri(vocabularyContext)));
        transactional(() -> {
            em.persist(vocabulary, descriptorFactory.vocabularyDescriptor(vocabulary));
            em.persist(vocabularyContext, descriptorFactory.vocabularyDescriptor(vocabularyContext.getUri()));
            em.persist(projectContext, descriptorFactory.projectContextDescriptor(projectContext));
            em.persist(publicationContext, descriptorFactory.publicationContextDescriptor(publicationContext));
        });
    }

    @Test
    @WithMockUser
    void getReadonlyPublicationContexts() {
        List<PublicationContextDto> readonlyPublicationContexts = sut.getReadonlyPublicationContexts();
        Assertions.assertEquals(readonlyPublicationContexts.size(), 1);
        Assertions.assertEquals(readonlyPublicationContexts.get(0).getUri(), publicationContext.getUri());
    }

    @Test
    void forMartin() {
        Change change = Generator.generateCreateChangeWitUri(vocabularyContext);
        change.setObject(new ChangeObject("ChangeValue", null, null));
        PublicationContext publicationContextWithRealChange = Generator.generatePublicationContextWithUri();
        publicationContextWithRealChange.setFromProject(projectContext);
        publicationContextWithRealChange.setChanges(Collections.singleton(change));
        transactional(() -> em.persist(publicationContextWithRealChange));
    }
}