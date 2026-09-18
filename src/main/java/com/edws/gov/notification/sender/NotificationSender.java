package com.edws.gov.notification.sender;

import com.edws.gov.notification.NotificationChannel;
import com.edws.gov.notification.NotificationRequest;

public interface NotificationSender {

    NotificationChannel supports();
    void send(NotificationRequest request, String body);
}
