package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.UserDao;
import com.github.checkit.dto.UserDto;
import com.github.checkit.model.User;
import com.github.checkit.util.TermVocabulary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class UserService extends BaseRepositoryService<User> {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    protected BaseDao<User> getPrimaryDao() {
        return userDao;
    }

    public UserDto getCurrent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = findByUserId(auth.getName());
        return new UserDto(user, auth.getAuthorities());
    }

    public User findByUserId(String userId) {
        URI userUri = URI.create(TermVocabulary.UZIVATEL_ID_PREFIX + userId);
        return findRequired(userUri);
    }
}
