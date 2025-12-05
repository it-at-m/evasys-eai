package de.muenchen.evasys.configuration;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "evasys.notification")
@Validated
public record NotificationProperties(
        @NotBlank String from,
        List<String> recipients) {
    public List<String> recipients() {
        return recipients == null ? List.of() : List.copyOf(recipients);
    }
}
