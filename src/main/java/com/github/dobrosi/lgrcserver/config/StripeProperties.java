package com.github.dobrosi.lgrcserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    private Api api = new Api();
    private Map<String, Price> prices;

    @Data
    public static class Api {
        private String secretKey;
        private String webhookSigningSecret;
    }

    @Data
    public static class Price {
        private String id;
        private boolean subscription;
    }
}
