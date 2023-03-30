package com.github.checkit.service;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.UserDao;
import com.github.checkit.dto.CurrentUserDto;
import com.github.checkit.model.User;
import java.net.URI;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseRepositoryService<User> {

    private final UserDao userDao;

    private final RepositoryConfigProperties repositoryConfigProperties;

    public UserService(UserDao userDao, RepositoryConfigProperties repositoryConfigProperties) {
        this.userDao = userDao;
        this.repositoryConfigProperties = repositoryConfigProperties;
    }

    @Override
    protected BaseDao<User> getPrimaryDao() {
        return userDao;
    }

    /**
     * Returns current user with his roles.
     *
     * @return current user
     */
    public CurrentUserDto getCurrentDto() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = getCurrent();
        return new CurrentUserDto(user, auth);
    }

    public User getCurrent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return findRequiredByUserId(auth.getName());
    }

    public User findRequiredByUserId(String userId) {
        URI userUri = createUserUriFromId(userId);
        return findRequired(userUri);
    }

    public User getRequiredReferenceByUserId(String userId) {
        URI userUri = createUserUriFromId(userId);
        return getRequiredReference(userUri);
    }

    private URI createUserUriFromId(String userId) {
        return URI.create(repositoryConfigProperties.getUser().getIdPrefix() + userId);
    }
}
