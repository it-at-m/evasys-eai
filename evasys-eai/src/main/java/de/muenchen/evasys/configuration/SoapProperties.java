package de.muenchen.evasys.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "soap")
@Validated
public record SoapProperties(
        @NotBlank String endpointUrl,
        @NotBlank String username,
        @NotBlank String password) {
}
