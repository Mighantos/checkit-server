package com.github.checkit.controller;

import com.github.checkit.dto.UserDto;
import com.github.checkit.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserController.MAPPING)
public class UserController extends BaseController {

    public static final String MAPPING = "/users";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/current")
    public UserDto getCurrentUser() {
        return userService.getCurrent();
    }
}
