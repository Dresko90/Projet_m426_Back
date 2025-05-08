package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;

public class ParticipantsResponseDto extends CollectionModel<ParticipantResponseDto> {
    public ParticipantsResponseDto() {
        super();
    }

    public ParticipantsResponseDto(List<ParticipantResponseDto> content, Link... links) {
        super(content, List.of(links), null);
    }
}
