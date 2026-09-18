package com.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
        // classe conteneur, pas d'instance
    }

    public record RegisterRequest(
            @NotBlank(message = "Le nom est obligatoire")
            @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
            String name,

            @NotBlank(message = "L'email est obligatoire")
            @Email(message = "Format d'email invalide")
            String email,

            @NotBlank(message = "Le mot de passe est obligatoire")
            @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
            String password
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "L'email est obligatoire")
            @Email(message = "Format d'email invalide")
            String email,

            @NotBlank(message = "Le mot de passe est obligatoire")
            String password
    ) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }

    // jamais le mot de passe ici, même hashé
    public record UserResponse(Long id, String name, String email) {
    }
}
