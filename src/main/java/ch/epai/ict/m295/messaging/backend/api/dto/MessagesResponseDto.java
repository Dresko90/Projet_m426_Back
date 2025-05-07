package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

public class MessagesResponseDto extends PagedModel<MessageResponseDto> {
    public MessagesResponseDto() {
        super();
    }

    public MessagesResponseDto(Collection<MessageResponseDto> content, PageMetadata metadata, Link... links) {
        super(content, metadata, List.of(links));
    }
}
