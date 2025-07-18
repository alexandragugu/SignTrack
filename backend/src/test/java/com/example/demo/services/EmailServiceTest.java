package com.example.demo.services;

import com.example.demo.utils.FileActionType;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TokenService tokenService;

    @Spy
    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "emailAddress", "signtrack@gmail.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "https://localhost");
    }

    @Test
    void testSendEmailNotification() throws Exception {
        UUID fileId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        Properties props = new Properties();
        Session session = Session.getInstance(props);
        MimeMessage mimeMessage = new MimeMessage(session);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        when(tokenService.generateFileToken(anyString(), anyString(), anyString(), any())).thenReturn("secure-token");

        doReturn("<html><body>{{messageContent}}<a href=\"{{secureLink}}\">Link</a></body></html>")
                .when(emailService).loadTemplate("templates/emailTemplate.html");

        doReturn("body { color: red; }")
                .when(emailService).loadTemplate("templates/emailStyle.css");

        ReflectionTestUtils.setField(emailService, "NotificationSubject", "Notification");

        emailService.sendEmailNotification(
                "alexandragugu@gmail.com",
                "randomGeneratedUUDI",
                "document.pdf",
                FileActionType.TO_SIGN,
                fileId,
                receiverId
        );

        verify(mailSender).send(any(MimeMessage.class));
    }
}