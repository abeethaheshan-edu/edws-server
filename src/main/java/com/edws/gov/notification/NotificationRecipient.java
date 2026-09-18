package com.edws.gov.notification;

import lombok.Builder;
import lombok.Data;

/**
 * Who the message goes to.
 *
 * <p>Carries every address, and {@link #addressFor} picks the one the channel needs. That is
 * what lets a caller build one recipient and reuse it whichever channel they pick.</p>
 */
@Data
@Builder
public class NotificationRecipient {

    private String userId;
    private String name;
    private String email;
    private String phone;
    private String deviceToken;

    public String addressFor(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> email;
            case SMS -> phone;
            case PUSH -> deviceToken;
        };
    }
}
