package ch.epai.ict.m295.messaging.backend.domain;

public class UserBuilder {
    private Long id;
    private String username;
    private String displayName; 
    private UserRoles role;

    public static UserBuilder create() {
        return new UserBuilder();
    }

    private UserBuilder() {
        this.role = UserRoles.USER;
    }

    public UserBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public UserBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public UserBuilder setRole(UserRoles role) {
        this.role = role;
        return this;
    }

    public User build() {
        if (id == null) {
            id = IdGeneratorManager.get(User.class).getNextId();
        }
        return new User(id, username, displayName, role);
    }
}
