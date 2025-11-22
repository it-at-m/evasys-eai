package de.muenchen.evasys.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "evasys")
@Validated
public record EvaSysProperties(
        @NotBlank String uri,
        @NotBlank String username,
        @NotBlank String password) {
}
