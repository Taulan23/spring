package ru.practicum.statsclient;

import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class StatsClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${stats-server.url:http://localhost:9090}")
    private String serverUrl;
    
    public StatsClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }
    
    public void hit(EndpointHit endpointHit) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<EndpointHit> request = new HttpEntity<>(endpointHit, headers);
        
        restTemplate.postForEntity(serverUrl + "/hit", request, Void.class);
    }
    
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        StringBuilder urlBuilder = new StringBuilder(serverUrl + "/stats?");
        urlBuilder.append("start=").append(URLEncoder.encode(start.format(formatter), StandardCharsets.UTF_8));
        urlBuilder.append("&end=").append(URLEncoder.encode(end.format(formatter), StandardCharsets.UTF_8));
        urlBuilder.append("&unique=").append(unique);
        
        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                urlBuilder.append("&uris=").append(URLEncoder.encode(uri, StandardCharsets.UTF_8));
            }
        }
        
        ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(urlBuilder.toString(), ViewStats[].class);
        
        return List.of(response.getBody());
    }
}
