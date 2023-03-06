package com.github.checkit.controller;

import com.github.checkit.dto.VocabularyDto;
import com.github.checkit.service.VocabularyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(VocabularyController.MAPPING)
public class VocabularyController extends BaseController {

    public static final String MAPPING = "/vocabularies";

    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @GetMapping
    public List<VocabularyDto> getAllVocabularies() {
        return vocabularyService.getAllInDto();
    }
}
