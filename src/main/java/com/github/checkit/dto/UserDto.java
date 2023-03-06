package com.github.checkit.dto;

import com.github.checkit.model.User;
import lombok.Getter;

@Getter
public class UserDto {

    private final String id;
    private final String firstName;
    private final String lastName;

    public UserDto(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UserDto(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }
}
