package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.ProjectContextDao;
import com.github.checkit.model.ProjectContext;
import org.springframework.stereotype.Service;

@Service
public class ProjectContextService extends BaseRepositoryService<ProjectContext> {

    private final ProjectContextDao projectContextDao;

    public ProjectContextService(ProjectContextDao projectContextDao) {
        this.projectContextDao = projectContextDao;
    }

    @Override
    protected BaseDao<ProjectContext> getPrimaryDao() {
        return projectContextDao;
    }
}
