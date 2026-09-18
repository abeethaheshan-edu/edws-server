package com.edws.gov.notification;

import com.edws.gov.notification.dispatcher.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

/**
 * The entry point callers use. Renders the template, then hands the finished body to the
 * dispatcher.
 *
 * <p>Rendering lives here rather than in the senders so that every channel is rendered the
 * same way, and a sender only has to know how to transmit a string.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    /** Exposed to templates as {@code ${content}} when the model is not a Map. */
    private static final String CONTENT_VARIABLE = "content";

    private final NotificationDispatcher dispatcher;

    @Qualifier("htmlTemplateEngine")
    private final TemplateEngine htmlTemplateEngine;

    @Qualifier("textTemplateEngine")
    private final TemplateEngine textTemplateEngine;

    /**
     * Renders and sends.
     *
     * <p>Runs on a background thread: an SMTP handshake, an SMS gateway call and an FCM
     * round trip all take seconds and fail regularly, and none of that belongs on a request
     * thread.</p>
     *
     * <p><b>Call this after your transaction commits.</b> Sending is not transactional and
     * no rollback recalls an SMS - publish a domain event and consume it with
     * {@code @TransactionalEventListener(phase = AFTER_COMMIT)} rather than calling this
     * from inside a {@code @Transactional} method.</p>
     *
     * <p>Fire and forget: because it is async, failures are logged, not thrown. The caller
     * has already committed and cannot act on them.</p>
     */
    @Async
    public void send(NotificationRequest request) {
        validate(request);

        String address = request.getRecipient().addressFor(request.getChannel());
        if (address == null || address.isBlank()) {
            // Normal, not an error: a citizen with no device token still gets the email.
            log.debug("Recipient has no {} address - skipping '{}'",
                    request.getChannel(), request.getTemplate());
            return;
        }

        try {
            String body = render(request);
            dispatcher.dispatch(request, body);
        } catch (Exception ex) {
            // Message only, never the cause chain: a Thymeleaf error can echo back variable
            // values, and for credential templates those values are passwords.
            log.error("Failed to send '{}' over {}: {}",
                    request.getTemplate(), request.getChannel(), ex.getMessage());
        }
    }

    private String render(NotificationRequest request) {
        NotificationChannel channel = request.getChannel();
        Context context = new Context(Locale.getDefault());

        if (request.getContent() instanceof Map<?, ?> map) {
            map.forEach((key, value) -> context.setVariable(String.valueOf(key), value));
        } else if (request.getContent() != null) {
            context.setVariable(CONTENT_VARIABLE, request.getContent());
        }

        return engineFor(channel).process(channel.templatePath(request.getTemplate()), context);
    }

    /**
     * HTML for email, text for the rest.
     *
     * <p>Thymeleaf fixes template mode on the resolver, not per call. Rendering an SMS
     * through the HTML engine would escape an apostrophe in a citizen's name into
     * {@code &#39;} inside a text message.</p>
     */
    private TemplateEngine engineFor(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL ? htmlTemplateEngine : textTemplateEngine;
    }

    private void validate(NotificationRequest request) {
        if (request.getTemplate() == null || request.getTemplate().isBlank()) {
            throw new IllegalArgumentException("template is required");
        }
        if (request.getChannel() == null) {
            throw new IllegalArgumentException("channel is required");
        }
        if (request.getRecipient() == null) {
            throw new IllegalArgumentException("recipient is required");
        }
    }
}
