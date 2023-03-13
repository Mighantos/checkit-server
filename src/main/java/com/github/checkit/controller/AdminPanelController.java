package com.github.checkit.controller;

import com.github.checkit.dto.AdminPanelSummaryDto;
import com.github.checkit.service.AdminPanelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AdminPanelController.MAPPING)
public class AdminPanelController extends AdminManagementController {

    public static final String MAPPING = AdminManagementController.MAPPING;

    private final AdminPanelService adminPanelService;

    public AdminPanelController(AdminPanelService adminPanelService) {
        this.adminPanelService = adminPanelService;
    }

    @GetMapping("/summary")
    public AdminPanelSummaryDto getSummary() {
        return adminPanelService.getSummary();
    }
}
