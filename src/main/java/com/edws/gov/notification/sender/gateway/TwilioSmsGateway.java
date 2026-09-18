package com.edws.gov.notification.sender.gateway;

import com.edws.gov.config.NotificationProperties;
import com.edws.gov.notification.sender.SmsSender;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@ConditionalOnProperty(name = "edws.notification.sms.enabled", havingValue = "true")
public class TwilioSmsGateway implements SmsSender.Gateway {
    private final NotificationProperties.Sms properties;

    public TwilioSmsGateway(NotificationProperties properties) {
        this.properties = properties.sms();

        require(this.properties.accountSid(), "edws.notification.sms.account-sid");
        require(this.properties.authToken(), "edws.notification.sms.auth-token");
        require(this.properties.fromNumber(), "edws.notification.sms.from-number");

        Twilio.init(this.properties.accountSid(), this.properties.authToken());
        log.info("Twilio SMS gateway initialised, sending from {}", this.properties.fromNumber());
    }

    @Override
    public void send(String phoneNumber, String message) {
        String to = toE164(phoneNumber);
        Message.creator(new PhoneNumber(to),
                        new PhoneNumber(properties.fromNumber()),
                        message)
                .create();
    }

    private String toE164(String phoneNumber) {
        String digits = phoneNumber.replaceAll("[\\s()-]", "");

        if (digits.startsWith("+")) {
            return digits;
        }
        if (digits.startsWith("0")) {
            return properties.defaultCountryCode() + digits.substring(1);
        }
        return properties.defaultCountryCode() + digits;
    }

    private void require(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "SMS is enabled but " + key + " is not set. " + "Set it in the environment, or set edws.notification.sms.enabled=false.");
        }
    }
}
