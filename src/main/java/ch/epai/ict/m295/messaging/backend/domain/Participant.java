package ch.epai.ict.m295.messaging.backend.domain;

public class Participant {

    public enum Role {
        OWNER,
        MEMBER
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        BLOCKED,
        INVITED
    }

    private long userId;
    private String username;
    private Role role;
    private Status status;

    public Participant(long userId, String username, Role role, Status status) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    public long getUserId() {
        return this.userId;
    }

    public String getUserName() {
        return this.username;
    }

    public Role getRole() {
        return this.role;
    }

    public Status getStatus() {
        return this.status;
    }

    @Override
    public String toString() {
        return "Participant{" +
                "id=" + this.userId +
                ", username='" + this.username + '\'' +
                ", role=" + this.role +
                ", status=" + this.status +
                '}';
    }   
}
