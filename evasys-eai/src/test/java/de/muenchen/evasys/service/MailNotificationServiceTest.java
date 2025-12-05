package de.muenchen.evasys.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.evasys.configuration.NotificationProperties;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class MailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationProperties props;

    @InjectMocks
    private MailNotificationService mailNotificationService;

    private MimeMessage realMimeMessage;

    @BeforeEach
    void setup() {
        realMimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);

        when(props.from()).thenReturn("sender@example.com");
        when(props.recipients()).thenReturn(List.of("recipient@example.com"));
    }

    static class TestRequest {
        public String id;
        public int value;

        public TestRequest(String id, int value) {
            this.id = id;
            this.value = value;
        }
    }

    private String getSentMessageContent() throws Exception {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage sent = captor.getValue();
        return (String) sent.getContent();
    }

    @Test
    void testNotifyErrorSendsMail() throws Exception {
        mailNotificationService.notifyError("Test Subject", "Some error", null, "Request data");

        String content = getSentMessageContent();
        assertTrue(content.contains("Some error"));
        assertTrue(content.contains("Request data"));
    }

    @Test
    void testNotifyErrorWithThrowableSendsMail() throws Exception {
        Exception ex = new Exception("Boom!");
        mailNotificationService.notifyError("Test Subject", "Some error", ex, "Request data");

        String content = getSentMessageContent();
        assertTrue(content.contains("Boom!"));
        assertTrue(content.contains("Request data"));
    }

    @Test
    void testNotifyErrorHandlesNullsGracefully() throws Exception {
        mailNotificationService.notifyError("Test Subject", null, null, null);

        String content = getSentMessageContent();
        assertTrue(content.contains("keine Request-Daten verfügbar"));
        assertTrue(content.contains("keine Stacktrace verfügbar"));
    }
}
