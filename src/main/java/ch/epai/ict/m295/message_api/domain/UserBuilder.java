package ch.epai.ict.m295.message_api.domain;

public class UserBuilder {
    private long id;
    private String email;
    private String displayName; 

    public static UserBuilder create() {
        return new UserBuilder();
    }

    public UserBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public UserBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public User build() {
        return new User(id, email, displayName);
    }

}
