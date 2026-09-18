package com.edws.gov.notification.sender;

import com.edws.gov.notification.NotificationChannel;
import com.edws.gov.notification.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsSender implements NotificationSender {

    private final Gateway gateway;

    @Override
    public NotificationChannel supports() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(NotificationRequest request, String body) {
        String phone = request.getRecipient().getPhone();
        gateway.send(phone, body);
        log.info("SMS '{}' sent to {}", request.getTemplate(), phone);
    }

    public interface Gateway {
        void send(String phoneNumber, String message);
    }
}
