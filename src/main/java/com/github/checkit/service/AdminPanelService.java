package com.github.checkit.service;

import com.github.checkit.dto.AdminPanelSummaryDto;
import org.springframework.stereotype.Service;

@Service
public class AdminPanelService {

    private final GestoringRequestService gestoringRequestService;
    private final VocabularyService vocabularyService;
    private final AdminUserService adminUserService;

    /**
     * Constructor.
     */
    public AdminPanelService(GestoringRequestService gestoringRequestService, VocabularyService vocabularyService,
                             AdminUserService adminUserService) {
        this.gestoringRequestService = gestoringRequestService;
        this.vocabularyService = vocabularyService;
        this.adminUserService = adminUserService;
    }

    /**
     * Returns summary of the state of the repository: number of pending gestoring requests, number of vocabularies with
     * at least one gestor, number of all canonical vocabularies and the number of admin users.
     *
     * @return {@link AdminPanelSummaryDto}
     */
    public AdminPanelSummaryDto getSummary() {
        int gestoringRequestCount = gestoringRequestService.getAllCount();
        int vocabularyWithGestorCount = vocabularyService.getGestoredCount();
        int vocabularyCount = vocabularyService.getAllCount();
        int adminCount = adminUserService.getAllAdminCount();
        return new AdminPanelSummaryDto(gestoringRequestCount, vocabularyWithGestorCount, vocabularyCount, adminCount);
    }
}
