package ch.epai.ict.m295.message_api.domain;

public class User {
    private long id;
    private String email;
    private String displayName;
    private UserRoles role;

    public User(long id, String email, String displayName, UserRoles role) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
    }

    public long getId() {
        return this.id;
    }
    
    public String getEmail() {
        return this.email;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public UserRoles getRole() {
        return this.role;
    }
}
