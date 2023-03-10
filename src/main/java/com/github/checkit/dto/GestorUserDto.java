package com.github.checkit.dto;

import com.github.checkit.model.User;
import java.net.URI;
import java.util.Set;
import lombok.Getter;

@Getter
public class GestorUserDto extends UserDto {
    private final String username;
    private final String email;
    private final boolean admin;
    private final Set<URI> gestoredVocabularies;

    public GestorUserDto(User user, String email, String username, boolean admin, Set<URI> gestoredVocabularies) {
        super(user);
        this.username = username;
        this.email = email;
        this.admin = admin;
        this.gestoredVocabularies = gestoredVocabularies;
    }
}
