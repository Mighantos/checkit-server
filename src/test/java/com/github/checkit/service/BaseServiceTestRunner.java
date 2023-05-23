package com.github.checkit.service;

import com.github.checkit.config.PersistenceConfig;
import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.environment.TestPersistenceFactory;
import com.github.checkit.environment.TransactionalTestRunner;
import com.github.checkit.persistence.DescriptorFactory;
import com.github.checkit.util.KeycloakApiUtil;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement
@ExtendWith(SpringExtension.class)
@ComponentScan(basePackages = {"com.github.checkit.service", "com.github.checkit.dao"})
@ContextConfiguration(initializers = {ConfigDataApplicationContextInitializer.class},
    classes = {
        PersistenceConfig.class,
        TestPersistenceFactory.class,
        DescriptorFactory.class,
        RepositoryConfigProperties.class,
        KeycloakApiUtil.class
    })
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BaseServiceTestRunner extends TransactionalTestRunner {

    private static final String EXISTENCE_CHECK_QUERY = "ASK { ?x a ?type . }";

    protected void verifyInstancesDoNotExist(String type, EntityManager em) {
        Assertions.assertFalse(
            em.createNativeQuery(EXISTENCE_CHECK_QUERY, Boolean.class).setParameter("type", URI.create(type))
                .getSingleResult());
    }
}
