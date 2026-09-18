package com.edws.gov.notification.sender;

import com.edws.gov.notification.NotificationChannel;
import com.edws.gov.notification.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushSender implements NotificationSender {

    private final Gateway gateway;

    @Override
    public NotificationChannel supports() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void send(NotificationRequest request, String body) {
        gateway.send(request.getRecipient().getDeviceToken(), request.getSubject(), body);
        log.info("Push '{}' sent", request.getTemplate());
    }


    public interface Gateway {
        void send(String deviceToken, String title, String body);
    }
}
