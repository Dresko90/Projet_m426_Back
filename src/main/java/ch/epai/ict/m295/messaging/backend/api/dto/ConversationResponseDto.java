package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "conversations")
public class ConversationResponseDto extends CollectionModel<ParticipantResponseDto> {
    private long id;
    private boolean isGroup;

    public ConversationResponseDto(long id, boolean isGroup, List<ParticipantResponseDto> participants, Link... links) {
        super(participants, List.of(links), null);
        this.id = id;
        this.isGroup = isGroup;
    }

    public long getId() {
        return id;
    }
 
    public boolean isGroup() {
        return isGroup;
    }
}
