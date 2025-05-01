package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.List;
import java.util.Map;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;

@Relation(collectionRelation = "conversations")
public class ConversationResponseDto extends RepresentationModel<ConversationResponseDto> {
    private long id;
    private boolean isGroup;
    private List<EntityModel<ParticipantResponseDto>> participants;

    public ConversationResponseDto(long id, boolean isGroup, List<EntityModel<ParticipantResponseDto>> participants) {
        this.id = id;
        this.isGroup = isGroup;
        this.participants = participants;
    }

    public long getId() {
        return id;
    }
 
    public boolean isGroup() {
        return isGroup;
    }

    @JsonProperty("_embedded")
    public Map<String, List<EntityModel<ParticipantResponseDto>>> getEmbedded() {
        return Map.of("participants", participants);
    } 
}
