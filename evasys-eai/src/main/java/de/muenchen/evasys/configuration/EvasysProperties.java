package de.muenchen.evasys.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "evasys")
@Validated
public record EvasysProperties(
        @NotBlank String uri,
        @NotBlank String username,
        @NotBlank String password,
        @NotNull @DefaultValue("10s") Duration connectionTimeout,
        @NotNull @DefaultValue("30s") Duration receiveTimeout,
        @NotBlank String defaultTeilbereichId) {
}
