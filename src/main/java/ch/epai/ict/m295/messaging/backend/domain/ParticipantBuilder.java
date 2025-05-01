package ch.epai.ict.m295.messaging.backend.domain;

public class ParticipantBuilder {
    private long id;
    private String username;
    private Participant.Role role;
    private Participant.Status status;

    public static ParticipantBuilder create() {
        return new ParticipantBuilder();
    }

    public ParticipantBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public ParticipantBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public ParticipantBuilder setRole(Participant.Role role) {
        this.role = role;
        return this;
    }

    public ParticipantBuilder setStatus(Participant.Status status) {
        this.status = status;
        return this;
    }

    public Participant build() {
        if (this.username == null || this.username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (this.role == null) {
            this.role = Participant.Role.MEMBER;
        }
        if (this.status == null) {
            this.status = Participant.Status.ACTIVE;
        }
        return new Participant(this.id, this.username, this.role, this.status);
    }
}
