package ch.epai.ict.m295.message_api.domain;

public class User {
    private long id;
    private String email;
    private String displayName;

    public User(long id, String email, String displayName) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }
}
