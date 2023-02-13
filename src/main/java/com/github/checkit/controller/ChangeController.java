package com.github.checkit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ChangeController {
    @GetMapping
    public String helloWorld(){
        return "hello world";
    }
}
