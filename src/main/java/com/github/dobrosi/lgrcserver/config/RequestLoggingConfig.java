package com.github.dobrosi.lgrcserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);      // Bekapcsolja a request body logolását
        filter.setMaxPayloadLength(10000);   // Limitálja a méretet, hogy ne szemetelje tele a logot
        filter.setIncludeHeaders(false);     // Állítsd true-ra, ha pl. az Authorization headert is látni akarod
        filter.setIncludeClientInfo(true);   // Kliens IP címe és session ID
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }
}
