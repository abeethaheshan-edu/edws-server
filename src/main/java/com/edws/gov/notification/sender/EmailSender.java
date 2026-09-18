package com.edws.gov.notification.sender;

import com.edws.gov.notification.NotificationChannel;
import com.edws.gov.notification.NotificationRequest;
import jakarta.mail.internet.MimeMessage;
import com.edws.gov.config.NotificationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class EmailSender implements NotificationSender {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailSender(JavaMailSender mailSender, NotificationProperties properties) {
        this.mailSender = mailSender;
        this.from = properties.email().from();
    }

    @Override
    public NotificationChannel supports() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationRequest request, String body) {
        String to = request.getRecipient().getEmail();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(request.getSubject() == null ? "" : request.getSubject());
            helper.setText(body, true);

            mailSender.send(message);
            log.info("Email '{}' sent to {}", request.getTemplate(), to);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send email to " + to, ex);
        }
    }
}
