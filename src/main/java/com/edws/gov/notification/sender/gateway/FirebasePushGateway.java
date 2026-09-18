package com.edws.gov.notification.sender.gateway;

import com.edws.gov.config.NotificationProperties;
import com.edws.gov.notification.sender.PushSender;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;


@Slf4j
@Component
@ConditionalOnProperty(name = "edws.notification.push.enabled", havingValue = "true")
public class FirebasePushGateway implements PushSender.Gateway {
    private final FirebaseApp firebaseApp;

    public FirebasePushGateway(NotificationProperties properties, ResourceLoader resourceLoader) {
        String credentialsPath = properties.push().credentialsPath();

        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalStateException(
                    "Push is enabled but edws.notification.push.credentials-path is not set.");
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            this.firebaseApp = FirebaseApp.getInstance();
            return;
        }

        try (InputStream credentials =
                     resourceLoader.getResource(credentialsPath).getInputStream()) {

            this.firebaseApp = FirebaseApp.initializeApp(FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build());
            log.info("Firebase push gateway initialised");

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Push is enabled but Firebase credentials could not be loaded from "
                            + credentialsPath, ex);
        }
    }

    @Override
    public void send(String deviceToken, String title, String body) {
        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(title == null ? "" : title)
                        .setBody(body)
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance(firebaseApp).send(message);
        } catch (FirebaseMessagingException ex) {
            MessagingErrorCode code = ex.getMessagingErrorCode();
            if (code == MessagingErrorCode.UNREGISTERED
                    || code == MessagingErrorCode.INVALID_ARGUMENT) {
                log.info("Device token no longer valid and should be removed from the user record");
                return;
            }
            throw new IllegalStateException("Firebase push failed: " + code, ex);
        }
    }
}
