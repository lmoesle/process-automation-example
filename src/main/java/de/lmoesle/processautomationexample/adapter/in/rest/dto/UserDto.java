package de.lmoesle.processautomationexample.adapter.in.rest.dto;

import de.lmoesle.processautomationexample.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserDto(
    @Schema(
        description = "Display name of the user.",
        example = "Ada Lovelace"
    )
    String name,
    @Schema(
        description = "Email address of the user.",
        example = "ada.lovelace@example.com"
    )
    String email
) {

    public static UserDto fromDomain(User user) {
        return new UserDto(
            user.name(),
            user.email()
        );
    }

    public static UserDto fromNullableDomain(User user) {
        return user == null ? null : fromDomain(user);
    }
}
