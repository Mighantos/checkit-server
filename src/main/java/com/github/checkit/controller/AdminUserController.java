package com.github.checkit.controller;

import com.github.checkit.service.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AdminUserController.MAPPING)
public class AdminUserController extends AdminManagementController {

    public static final String MAPPING = AdminManagementController.MAPPING + "/users";

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PutMapping("/{userKeycloakId}/admin-role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeRole(@PathVariable String userKeycloakId, @RequestBody boolean admin) {
        adminUserService.setAdminRoleToUser(userKeycloakId, admin);
    }
}
