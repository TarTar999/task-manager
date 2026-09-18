package com.taskmanager.exception;

import org.springframework.http.HttpStatus;

/** Erreur métier qu'on veut renvoyer telle quelle au client, avec son code HTTP. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
