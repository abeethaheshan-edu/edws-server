package com.edws.gov.notification;

/**
 * A delivery medium.
 *
 * <p>The lowercase name doubles as the template folder: {@code templates/email/otp.html},
 * {@code templates/sms/otp.txt}. Convention rather than a lookup table, so adding a message
 * means adding template files and nothing else.</p>
 */
public enum NotificationChannel {

    EMAIL("email", ".html"),
    SMS("sms", ".txt"),
    PUSH("push", ".txt");

    private final String folder;
    private final String suffix;

    NotificationChannel(String folder, String suffix) {
        this.folder = folder;
        this.suffix = suffix;
    }

    /** Thymeleaf template path for this channel, e.g. {@code email/otp}. */
    public String templatePath(String template) {
        return folder + "/" + template;
    }

    public String suffix() {
        return suffix;
    }
}
