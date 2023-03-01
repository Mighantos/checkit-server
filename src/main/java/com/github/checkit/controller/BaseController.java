package com.github.checkit.controller;

import com.github.checkit.security.UserRole;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('" + UserRole.USER + "')")
public class BaseController {
}