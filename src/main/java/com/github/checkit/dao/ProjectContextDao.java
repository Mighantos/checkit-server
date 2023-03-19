package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.persistence.DescriptorFactory;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectContextDao extends BaseDao<ProjectContext> {

    private final DescriptorFactory descriptorFactory;

    protected ProjectContextDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(ProjectContext.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public Optional<ProjectContext> find(URI id) {
        Objects.requireNonNull(id);
        try {
            Descriptor descriptor = descriptorFactory.projectContextDescriptor(id);
            return Optional.ofNullable(em.find(type, id, descriptor));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
