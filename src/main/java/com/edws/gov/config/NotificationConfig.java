package com.edws.gov.config;

import com.edws.gov.notification.sender.PushSender;
import com.edws.gov.notification.sender.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;


@Slf4j
@Configuration
@EnableConfigurationProperties(NotificationProperties.class)
public class NotificationConfig {

    @Bean
    public SpringTemplateEngine htmlTemplateEngine() {
        return engine(TemplateMode.HTML, ".html");
    }

    @Bean
    public SpringTemplateEngine textTemplateEngine() {
        return engine(TemplateMode.TEXT, ".txt");
    }

    /**
     * Uses {@link SpringTemplateEngine}, not the plain {@code TemplateEngine}.
     *
     * <p>The plain engine installs Thymeleaf's {@code StandardDialect}, whose expression
     * evaluator is OGNL - and {@code thymeleaf-spring6} deliberately excludes OGNL from the
     * classpath, so the first render fails with {@code NoClassDefFoundError:
     * ognl/PropertyAccessor}. The Spring engine evaluates {@code ${...}} with SpEL instead,
     * which is already on the classpath.</p>
     */
    private SpringTemplateEngine engine(TemplateMode mode, String suffix) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(suffix);
        resolver.setTemplateMode(mode);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(true);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    @Bean
    @ConditionalOnMissingBean(SmsSender.Gateway.class)
    public SmsSender.Gateway loggingSmsGateway() {
        return (phoneNumber, message) -> log.warn("SMS NOT SENT - Twilio disabled. Recipient: {}", phoneNumber);
    }

    @Bean
    @ConditionalOnMissingBean(PushSender.Gateway.class)
    public PushSender.Gateway loggingPushGateway() {
        return (deviceToken, title, body) ->
                log.warn("PUSH NOT SENT - Firebase disabled. Title: {}", title);
    }
}
