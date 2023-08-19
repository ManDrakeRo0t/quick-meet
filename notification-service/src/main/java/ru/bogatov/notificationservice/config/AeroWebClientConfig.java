package ru.bogatov.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AeroWebClientConfig {

    @Value("${sms-aero.url}")
    private String aeroUrl;
    @Value("${sms-aero.username}")
    private String username;
    @Value("${sms-aero.key}")
    private String password;


    @Bean
    public WebClient aeroWebClient() {
        return WebClient.builder()
                .baseUrl(aeroUrl)
                .filter(ExchangeFilterFunctions.basicAuthentication(username, password))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
