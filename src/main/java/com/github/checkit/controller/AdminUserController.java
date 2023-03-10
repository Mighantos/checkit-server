package com.github.checkit.controller;

import com.github.checkit.dto.GestorUserDto;
import com.github.checkit.service.AdminUserService;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AdminUserController.MAPPING)
public class AdminUserController extends AdminManagementController {

    public static final String MAPPING = AdminManagementController.MAPPING + "/users";

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<GestorUserDto> getAllUsers() {
        return adminUserService.getAllUsers();
    }

    @PutMapping("/{userKeycloakId}/admin-role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeRole(@PathVariable String userKeycloakId, @RequestBody boolean admin) {
        adminUserService.setAdminRoleToUser(userKeycloakId, admin);
    }

    @PostMapping("/{userKeycloakId}/gestored-vocabulary")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addGestoredVocabulary(@PathVariable String userKeycloakId, @RequestBody URI vocabularyUri) {
        adminUserService.addUserAsGestorOfVocabulary(vocabularyUri, userKeycloakId);
    }

    @DeleteMapping("/{userKeycloakId}/gestored-vocabulary")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeGestoredVocabulary(@PathVariable String userKeycloakId, @RequestBody URI vocabularyUri) {
        adminUserService.removeUserAsGestorFromVocabulary(vocabularyUri, userKeycloakId);
    }
}
