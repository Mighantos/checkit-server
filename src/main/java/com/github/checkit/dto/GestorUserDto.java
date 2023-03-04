package com.github.checkit.dto;

import com.github.checkit.model.User;
import lombok.Getter;

import java.net.URI;
import java.util.Set;

@Getter
public class GestorUserDto {
    private final String id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final boolean admin;
    private final Set<URI> gestoredVocabularies;

    public GestorUserDto(User user, String email, String username, boolean admin, Set<URI> gestoredVocabularies) {
        this.id = user.getId();
        this.username = username;
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = email;
        this.admin = admin;
        this.gestoredVocabularies = gestoredVocabularies;
    }
}
