package com.edws.gov.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "edws.notification")
public record NotificationProperties(
        Email email,
        Sms sms,
        Push push
) {

    public NotificationProperties {
        email = email == null ? new Email(true, "no-reply@edws.gov.lk") : email;
        sms = sms == null ? new Sms(false, null, null, null, "+94") : sms;
        push = push == null ? new Push(false, null) : push;
    }

    public record Email(boolean enabled, String from) {}

    public record Sms(
            boolean enabled,
            String accountSid,
            String authToken,
            String fromNumber,
            String defaultCountryCode
    ) {
        public Sms {
            defaultCountryCode = (defaultCountryCode == null || defaultCountryCode.isBlank()) ? "+94" : defaultCountryCode;
        }
    }

    public record Push(boolean enabled, String credentialsPath) {}
}
