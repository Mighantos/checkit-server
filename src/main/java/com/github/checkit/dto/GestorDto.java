package com.github.checkit.dto;

import com.github.checkit.model.User;
import lombok.Getter;

import java.net.URI;
import java.util.Set;

@Getter
public class GestorDto {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final boolean admin;
    private final Set<URI> gestoredVocabularies;

    public GestorDto(User user, boolean admin, Set<URI> gestoredVocabularies) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.admin = admin;
        this.gestoredVocabularies = gestoredVocabularies;
    }
}
