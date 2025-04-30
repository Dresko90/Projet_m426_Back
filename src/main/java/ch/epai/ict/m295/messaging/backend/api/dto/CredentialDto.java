package ch.epai.ict.m295.messaging.backend.api.dto;

public class CredentialDto {
    private String username;
    private String password;

    public CredentialDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}