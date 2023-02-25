package com.github.checkit.service;

import com.github.checkit.dao.UserDao;
import com.github.checkit.dto.GestorDto;
import com.github.checkit.exception.KeycloakApiAdminException;
import com.github.checkit.util.KeycloakApiUtil;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final KeycloakApiUtil keycloakApiUtil;

    private final UserDao userDao;

    public AdminUserService(KeycloakApiUtil keycloakApiUtil, UserDao userDao) {
        this.keycloakApiUtil = keycloakApiUtil;
        this.userDao = userDao;
    }

    public void setAdminRoleToUser(String userKeycloakId, boolean admin) {
        checkNotApiAdmin(userKeycloakId);
        UserResource user = keycloakApiUtil.getApi().users().get(userKeycloakId);
        RoleScopeResource roleScopeResource = user.roles().clientLevel(keycloakApiUtil.getClientUUID());
        if (admin) {
            roleScopeResource.add(List.of(keycloakApiUtil.getAdminRole()));
        } else {
            roleScopeResource.remove(List.of(keycloakApiUtil.getAdminRole()));
        }
    }

    public List<GestorDto> getAllUsers() {
        return userDao.findAll().stream().filter(user -> !user.getId().equals(keycloakApiUtil.getApiAdminId()))
                .map(user -> {
                    boolean admin = keycloakApiUtil.getApi().users().get(user.getId())
                            .roles().clientLevel(keycloakApiUtil.getClientUUID()).listAll()
                            .contains(keycloakApiUtil.getAdminRole());
                    return new GestorDto(user, admin);
                }).collect(Collectors.toList());
    }

    private void checkNotApiAdmin(String id) {
        if (id.equals(keycloakApiUtil.getApiAdminId())) {
            throw new KeycloakApiAdminException();
        }
    }
}
