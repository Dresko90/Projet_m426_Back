package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "conversations")
public class ConversationResponseDto extends CollectionModel<EntityModel<ParticipantResponseDto>> {
    private long id;

    public ConversationResponseDto(long id, List<EntityModel<ParticipantResponseDto>> participants) {
        super(participants);
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
