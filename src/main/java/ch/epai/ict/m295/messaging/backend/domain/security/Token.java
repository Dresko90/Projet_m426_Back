package ch.epai.ict.m295.messaging.backend.domain.security;

import java.util.UUID;

public class Token {
    private String value;

    public static Token randomToken() {
        return new Token(UUID.randomUUID().toString());
    }

    public static Token fromString(String value) {
        return new Token(value);
    }

    private Token(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
