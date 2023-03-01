package com.github.checkit.controller;

import com.github.checkit.security.UserRole;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AdminManagementController.MAPPING)
@PreAuthorize("hasRole('" + UserRole.ADMIN + "')")
public class AdminManagementController {
    public static final String MAPPING = "/admin-management";
}
