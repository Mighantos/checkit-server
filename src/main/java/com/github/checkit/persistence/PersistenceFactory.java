package com.github.checkit.persistence;

import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.DATA_SOURCE_CLASS;
import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.LANG;
import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY;
import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.SCAN_PACKAGE;
import static cz.cvut.kbss.jopa.model.PersistenceProperties.JPA_PERSISTENCE_PROVIDER;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.kbss.ontodriver.rdf4j.config.Rdf4jOntoDriverProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PersistenceFactory {
    private final RepositoryConfigProperties repositoryConfigProperties;

    private EntityManagerFactory emf;

    public PersistenceFactory(RepositoryConfigProperties repositoryConfigProperties) {
        this.repositoryConfigProperties = repositoryConfigProperties;
    }

    /**
     * Default persistence unit configuration parameters.
     *
     * <p>These include: package scan for entities, provider specification
     *
     * @return Map with defaults
     */
    public static Map<String, String> defaultParams() {
        final Map<String, String> map = new HashMap<>();
        map.put(SCAN_PACKAGE, "com.github.checkit.model");
        map.put(JPA_PERSISTENCE_PROVIDER, JOPAPersistenceProvider.class.getName());
        return map;
    }

    @Bean
    @Primary
    public EntityManagerFactory entityManagerFactory() {
        return emf;
    }

    @PostConstruct
    private void init() {
        Map<String, String> emfProperties = defaultParams();
        emfProperties.put(ONTOLOGY_PHYSICAL_URI_KEY, repositoryConfigProperties.getUrl());
        emfProperties.put(DATA_SOURCE_CLASS, repositoryConfigProperties.getDriver());
        emfProperties.put(LANG, repositoryConfigProperties.getLanguage());
        emfProperties.put(Rdf4jOntoDriverProperties.LOAD_ALL_THRESHOLD, "1");
        this.emf = Persistence.createEntityManagerFactory("checkItPU", emfProperties);
    }

    @PreDestroy
    private void destroy() {
        if (Objects.nonNull(emf) && emf.isOpen()) {
            emf.close();
        }
    }
}
