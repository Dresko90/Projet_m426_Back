package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UsersResponseDto extends PagedModel<UserResponseDto> {
    
    public UsersResponseDto() {
        super();
    }

    public UsersResponseDto(Collection<UserResponseDto> content, PageMetadata metadata, Link... links) {
        super(content, metadata, List.of(links));
    }

    @JsonIgnore
    public UsersResponseDto addLink(Link link) {
        super.add(link);
        return this;
    }
}
