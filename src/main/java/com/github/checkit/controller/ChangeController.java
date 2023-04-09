package com.github.checkit.controller;

import com.github.checkit.service.ChangeService;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ChangeController.MAPPING)
public class ChangeController extends BaseController {

    public static final String MAPPING = "/changes";
    private final ChangeService changeService;

    public ChangeController(ChangeService changeService) {
        this.changeService = changeService;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{changeId}/approved")
    public void approveChange(@PathVariable String changeId) {
        changeService.approveChange(changeId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/approved")
    public void approveChanges(@RequestBody List<URI> changes) {
        changeService.approveChanges(changes);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{changeId}/rejected")
    public void rejectChange(@PathVariable String changeId) {
        changeService.rejectChange(changeId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/rejected")
    public void rejectChanges(@RequestBody List<URI> changes) {
        changeService.rejectChanges(changes);
    }
}
