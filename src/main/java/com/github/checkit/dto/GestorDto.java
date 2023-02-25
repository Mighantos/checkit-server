package com.github.checkit.dto;

import com.github.checkit.model.User;
import lombok.Getter;

@Getter
public class GestorDto {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final boolean admin;

    public GestorDto(User user, boolean admin) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.admin = admin;
    }
}
