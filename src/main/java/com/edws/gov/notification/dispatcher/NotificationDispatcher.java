package com.edws.gov.notification.dispatcher;

import com.edws.gov.notification.NotificationChannel;
import com.edws.gov.notification.NotificationRequest;
import com.edws.gov.notification.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationDispatcher {

    private final Map<NotificationChannel, NotificationSender> senders;

    public NotificationDispatcher(List<NotificationSender> senders) {
        this.senders = senders.stream().collect(Collectors.toMap(
                NotificationSender::supports,
                Function.identity(),
                (a, b) -> {
                    throw new IllegalStateException( "Two senders registered for channel " + a.supports());
                },
                () -> new EnumMap<>(NotificationChannel.class)));

        log.info("Notification channels available: {}", this.senders.keySet());
    }

    public void dispatch(NotificationRequest request, String body) {
        NotificationSender sender = senders.get(request.getChannel());

        if (sender == null) {
            log.warn("No sender registered for {} - '{}' not delivered",
                    request.getChannel(), request.getTemplate());
            return;
        }
        sender.send(request, body);
    }
}
