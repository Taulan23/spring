package ru.practicum.mainservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.statsclient.StatsClient;

@Configuration
public class StatsClientConfig {
    
    @Value("${stats.server.url:http://localhost:9090}")
    private String statsServerUrl;
    
    @Bean
    public StatsClient statsClient(RestTemplateBuilder restTemplateBuilder) {
        return new StatsClient(restTemplateBuilder, statsServerUrl);
    }
}
