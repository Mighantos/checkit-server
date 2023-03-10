package com.github.checkit.controller;

import com.github.checkit.dto.GestoringRequestDto;
import com.github.checkit.security.UserRole;
import com.github.checkit.service.GestoringRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(GestoringRequestController.MAPPING)
public class GestoringRequestController extends BaseController {

    public static final String MAPPING = "/gestoring-requests";

    private final GestoringRequestService gestoringRequestService;

    public GestoringRequestController(GestoringRequestService gestoringRequestService) {
        this.gestoringRequestService = gestoringRequestService;
    }

    @GetMapping
    @PreAuthorize("hasRole('" + UserRole.ADMIN + "')")
    public List<GestoringRequestDto> getAllGestoringRequests() {
        return gestoringRequestService.findAllAsDtos();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping
    public void createGestoringRequest(@RequestParam URI vocabularyUri) {
        gestoringRequestService.create(vocabularyUri);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{requestId}")
    public void removeGestoringRequest(@PathVariable String requestId) {
        gestoringRequestService.remove(requestId);
    }
}
