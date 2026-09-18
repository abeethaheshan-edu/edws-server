package com.edws.gov.notification;

import lombok.Builder;
import lombok.Data;

/**
 * One message, on one channel, to one recipient.
 *
 * <pre>
 * NotificationRequest.builder()
 *         .template("otp")
 *         .channel(NotificationChannel.EMAIL)
 *         .recipient(recipient)
 *         .subject("Your verification code")
 *         .content(new OtpContent(code, minutes))
 *         .build();
 * </pre>
 *
 * <p>Singular channel and recipient on purpose. A request that fans out to several channels
 * has no single outcome - it half-succeeds, and the caller cannot tell which part failed.
 * One request, one delivery, one result. Send several if you need several.</p>
 */
@Data
@Builder
public class NotificationRequest {

    /**
     * Template name without folder or suffix, e.g. {@code "otp"}. The channel supplies the
     * rest: EMAIL resolves {@code templates/email/otp.html}, SMS {@code templates/sms/otp.txt}.
     */
    private String template;

    private NotificationChannel channel;

    private NotificationRecipient recipient;

    /**
     * The Thymeleaf model.
     *
     * <p>Pass a typed record and templates read {@code ${content.code}}; pass a
     * {@code Map<String, Object>} and its keys become top-level variables, {@code ${code}}.
     * A record is preferable - rename a field and the compiler finds every call site, where
     * a map key typo just renders blank.</p>
     */
    private Object content;

    /** Email subject, and the title of a push notification. Ignored by SMS. */
    private String subject;
}
