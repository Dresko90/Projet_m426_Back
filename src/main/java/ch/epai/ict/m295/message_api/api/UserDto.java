package ch.epai.ict.m295.message_api.api;

public class UserDto {
    public Long id;
    public String email;
    public String displayName;

    public UserDto(Long id, String email, String displayName) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
    }
}
