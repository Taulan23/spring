package ru.practicum.statsclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statsdto.EndpointHit;
import ru.practicum.statsdto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class StatsClient {
    
    private final RestTemplate restTemplate;
    private String serverUrl;
    
    public StatsClient() {
        this.restTemplate = new RestTemplate();
        this.serverUrl = "http://localhost:9090";
    }
    
    public StatsClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
        this.serverUrl = "http://localhost:9090";
    }
    
    public StatsClient(String serverUrl) {
        this.restTemplate = new RestTemplate();
        this.serverUrl = serverUrl;
    }
    
    public StatsClient(RestTemplateBuilder builder, String serverUrl) {
        this.restTemplate = builder.build();
        this.serverUrl = serverUrl;
    }
    
    public void hit(EndpointHit endpointHit) {
        if (endpointHit == null) {
            throw new RuntimeException("EndpointHit cannot be null");
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<EndpointHit> request = new HttpEntity<>(endpointHit, headers);
        
        restTemplate.postForEntity(serverUrl + "/hit", request, Void.class);
    }
    
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start == null || end == null) {
            throw new RuntimeException("Start and end dates cannot be null");
        }
        
        if (start.isAfter(end)) {
            throw new RuntimeException("Start date cannot be after end date");
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        StringBuilder urlBuilder = new StringBuilder(serverUrl + "/stats?");
        urlBuilder.append("start=").append(URLEncoder.encode(start.format(formatter), StandardCharsets.UTF_8));
        urlBuilder.append("&end=").append(URLEncoder.encode(end.format(formatter), StandardCharsets.UTF_8));
        urlBuilder.append("&unique=").append(unique);
        
        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                if (uri != null && !uri.trim().isEmpty()) {
                    urlBuilder.append("&uris=").append(URLEncoder.encode(uri, StandardCharsets.UTF_8));
                }
            }
        }
        
        ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(urlBuilder.toString(), ViewStats[].class);
        
        ViewStats[] body = response.getBody();
        if (body == null || body.length == 0) {
            return List.of();
        }
        
        return List.of(body);
    }
}
