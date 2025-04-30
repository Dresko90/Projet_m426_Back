package ch.epai.ict.m295.messaging.backend.domain;

public class User {
    private long id;
    private String username;
    private String displayName;
    private UserRoles role;

    public User(long id, String username, String displayName, UserRoles role) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
    }

    public long getId() {
        return this.id;
    }
    
    public String getUsername() {
        return this.username;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public UserRoles getRole() {
        return this.role;
    }
}
