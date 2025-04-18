package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.utils.MailBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private MailBody testMailBody;

    @BeforeEach
    void setUp() {
        // Setup test mail body
        testMailBody = new MailBody(
                "recipient@example.com",
                "Test Subject",
                "Test Body"
        );
    }

    @Nested
    @DisplayName("Test sendSimpleMail")
    class SendSimpleMailTest {
        @Test
        @DisplayName("Gửi email thành công")
        void sendSimpleMail_Success() {
            // Input: MailBody hợp lệ
            // Expected: Gửi email thành công

            // Arrange
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            // Act
            emailService.sendSimpleMail(testMailBody);

            // Assert
            verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Gửi email thất bại do lỗi mail sender")
        void sendSimpleMail_MailSenderError() {
            // Input: MailBody hợp lệ nhưng mail sender lỗi
            // Expected: Ném ra RuntimeException

            // Arrange
            doThrow(new RuntimeException("Mail sender error")).when(mailSender).send(any(SimpleMailMessage.class));

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> emailService.sendSimpleMail(testMailBody));
        }

        @Test
        @DisplayName("Gửi email với MailBody null")
        void sendSimpleMail_NullMailBody() {
            // Input: MailBody null
            // Expected: Ném ra NullPointerException

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> emailService.sendSimpleMail(null));
        }

        @Test
        @DisplayName("Gửi email với recipient null")
        void sendSimpleMail_NullRecipient() {
            // Input: MailBody với recipient null
            // Expected: Ném ra NullPointerException

            // Arrange
            MailBody invalidMailBody = new MailBody(
                    null,
                    "Test Subject",
                    "Test Body"
            );

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> emailService.sendSimpleMail(invalidMailBody));
        }

        @Test
        @DisplayName("Gửi email với subject null")
        void sendSimpleMail_NullSubject() {
            // Input: MailBody với subject null
            // Expected: Ném ra NullPointerException

            // Arrange
            MailBody invalidMailBody = new MailBody(
                    "recipient@example.com",
                    null,
                    "Test Body"
            );

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> emailService.sendSimpleMail(invalidMailBody));
        }

        @Test
        @DisplayName("Gửi email với body null")
        void sendSimpleMail_NullBody() {
            // Input: MailBody với body null
            // Expected: Ném ra NullPointerException

            // Arrange
            MailBody invalidMailBody = new MailBody(
                    "recipient@example.com",
                    "Test Subject",
                    null
            );

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> emailService.sendSimpleMail(invalidMailBody));
        }
    }
}