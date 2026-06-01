package de.muenchen.evasys.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "sappo")
@Validated
public record SapPoProperties(
        @NotBlank String uri,
        @NotBlank String username,
        @NotBlank String password) {
}
