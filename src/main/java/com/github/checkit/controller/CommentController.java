package com.github.checkit.controller;

import com.github.checkit.dto.CommentDto;
import com.github.checkit.service.CommentService;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CommentController.MAPPING)
public class CommentController extends BaseController {

    public static final String MAPPING = "/comments";

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/discussion-on-change")
    public List<CommentDto> getAllRelatedToChange(@RequestParam URI changeUri) {
        return commentService.getAllRelatedToChange(changeUri);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping
    public void createComment(@RequestParam URI changeUri, @RequestBody String content) {
        commentService.createComment(changeUri, content);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/rejection-on-change")
    public void createRejectionComment(@RequestParam URI changeUri, @RequestBody String content) {
        commentService.createRejectionComment(changeUri, content);
    }
}
