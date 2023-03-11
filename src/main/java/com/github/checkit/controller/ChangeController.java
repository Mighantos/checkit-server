package com.github.checkit.controller;

import com.github.checkit.service.ChangeService;
import java.net.URI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ChangeController.MAPPING)
public class ChangeController extends BaseController {

    public static final String MAPPING = "/";

    private final ChangeService changeService;

    public ChangeController(ChangeService changeService) {
        this.changeService = changeService;
    }

    @GetMapping("/hello")
    public String helloWorld() {
        return "hello world";
    }

    @PostMapping(value = "/")
    public String printChanges(@RequestBody URI vocabularyContextUri) {
        return changeService.getChangesAsString(vocabularyContextUri);
    }
}
