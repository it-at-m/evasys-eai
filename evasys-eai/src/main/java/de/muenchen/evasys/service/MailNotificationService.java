package de.muenchen.evasys.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.configuration.NotificationProperties;
import jakarta.mail.internet.MimeMessage;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailNotificationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private final JavaMailSender mailSender;
    private final NotificationProperties notificationProperties;

    public MailNotificationService(final JavaMailSender mailSender, final NotificationProperties notificationProperties) {
        this.mailSender = mailSender;
        this.notificationProperties = notificationProperties;
    }

    public void notifyError(final String subject, final String errorMessage, final Throwable throwable, final Object requestObj) {
        try {
            final MimeMessage msg = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");

            helper.setFrom(notificationProperties.from());
            helper.setTo(notificationProperties.recipients().toArray(String[]::new));
            helper.setSubject("Fehler EvaSys-EAI");

            final String stackTrace = throwable != null ? toStackTrace(throwable) : "keine Stacktrace verfügbar";
            final String requestStr = formatRequest(requestObj);

            final String body = String.format(
                    "<h2>%s</h2>%n" +
                            "<p><strong>Fehlermeldung:</strong> %s</p>%n" +
                            "<p><strong>Stacktrace:</strong><br/>%s</p>%n" +
                            "<p><strong>Request-Daten:</strong></p>%n" +
                            "<pre>%s</pre>%n",
                    escapeHtml(subject),
                    escapeHtml(errorMessage),
                    escapeHtml(stackTrace),
                    escapeHtml(requestStr));

            helper.setText(body, true);
            mailSender.send(msg);

        } catch (Exception e) {
            LOGGER.error("Could not send error notification email");
        }
    }

    public static String toStackTrace(final Throwable throwable) {
        return throwable == null ? "keine Stacktrace verfügbar" : throwable.toString();
    }

    private String formatRequest(final Object requestObj) {
        if (requestObj == null) {
            return "keine Request-Daten verfügbar";
        }
        return switch (requestObj) {
        case ZLSOSTEVASYSRFC z -> truncate(formatZlso(z), 8000);
        default -> {
            try {
                yield truncate(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(requestObj), 4000);
            } catch (Exception e) {
                yield truncate(requestObj.toString(), 2000);
            }
        }
        };
    }

    private String formatZlso(final ZLSOSTEVASYSRFC trainingData) {
        final StringJoiner sj = new StringJoiner(", ");
        appendIfPresent(sj, "TRAININGID", trainingData.getTRAININGID());
        appendIfPresent(sj, "TRAININGTITEL", trainingData.getTRAININGTITEL());
        appendIfPresent(sj, "TRAININGSTYPKUERZEL", trainingData.getTRAININGSTYPKUERZEL());
        appendIfPresent(sj, "TRAININGART", trainingData.getTRAININGART());
        appendIfPresent(sj, "TRAININGBEGINN", trainingData.getTRAININGBEGINN());
        appendIfPresent(sj, "TRAININGENDE", trainingData.getTRAININGENDE());
        appendIfPresent(sj, "TRAININGDAUERTAGE", trainingData.getTRAININGDAUERTAGE());
        appendIfPresent(sj, "TRAININGDAUERSTD", trainingData.getTRAININGDAUERSTD());
        appendIfPresent(sj, "TRAININGRAUM", trainingData.getTRAININGRAUM());
        appendIfPresent(sj, "TRAININGTNANZAHL", trainingData.getTRAININGTNANZAHL());
        appendIfPresent(sj, "TEILBEREICH", trainingData.getTRAININGTEILBEREICH());
        appendIfPresent(sj, "TEILBEREICHID", trainingData.getTEILBEREICHID());
        appendIfPresent(sj, "TRAINER1ID", trainingData.getTRAINER1ID());
        appendIfPresent(sj, "TRAINER1TITEL", trainingData.getTRAINER1TITEL());
        appendIfPresent(sj, "TRAINER1VNAME", trainingData.getTRAINER1VNAME());
        appendIfPresent(sj, "TRAINER1NNAME", trainingData.getTRAINER1NNAME());
        appendIfPresent(sj, "TRAINER1MAIL", trainingData.getTRAINER1MAIL());
        appendIfPresent(sj, "SEKTRAINERID", trainingData.getSEKTRAINERID());
        appendIfPresent(sj, "FIRMA", trainingData.getFIRMA());
        return sj.toString();
    }

    private static void appendIfPresent(final StringJoiner sj, final String key, final String val) {
        if (val != null && !val.isBlank()) {
            sj.add(key + "=" + val);
        }
    }

    private static String truncate(final String s, final int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...(truncated)";
    }

    private static String escapeHtml(final String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
