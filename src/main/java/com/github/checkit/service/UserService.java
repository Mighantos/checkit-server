package com.github.checkit.service;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.UserDao;
import com.github.checkit.dto.UserDto;
import com.github.checkit.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URI;

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

    public UserDto getCurrent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = findRequiredByUserId(auth.getName());
        return new UserDto(user, auth.getAuthorities());
    }

    public User findRequiredByUserId(String userId) {
        URI userUri = createUserUriFromId(userId);
        return findRequired(userUri);
    }

    public User getRequiredReferenceByUserId(String userId) {
        URI userUri = createUserUriFromId(userId);
        return getRequiredReference(userUri);
    }

    private URI createUserUriFromId(String userId){
        return URI.create(repositoryConfigProperties.getUserIdPrefix() + userId);
    }
}
