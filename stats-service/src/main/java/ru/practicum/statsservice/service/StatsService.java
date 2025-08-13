package ru.practicum.statsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.statsservice.dto.ViewStatsDto;
import ru.practicum.statsservice.exception.ValidationException;
import ru.practicum.statsservice.model.EndpointHit;
import ru.practicum.statsservice.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {
    
    private final EndpointHitRepository repository;
    
    public EndpointHit saveHit(EndpointHit hit) {
        if (hit.getApp() == null || hit.getApp().trim().isEmpty()) {
            throw new ValidationException("App cannot be empty");
        }
        if (hit.getUri() == null || hit.getUri().trim().isEmpty()) {
            throw new ValidationException("Uri cannot be empty");
        }
        if (hit.getIp() == null || hit.getIp().trim().isEmpty()) {
            throw new ValidationException("Ip cannot be empty");
        }
        if (hit.getTimestamp() == null) {
            throw new ValidationException("Timestamp cannot be null");
        }
        
        hit.setApp(hit.getApp().trim());
        hit.setUri(hit.getUri().trim());
        hit.setIp(hit.getIp().trim());
        
        return repository.save(hit);
    }
    
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start == null || end == null) {
            throw new ValidationException("Start and end dates cannot be null");
        }
        
        if (start.isAfter(end)) {
            throw new ValidationException("Start date cannot be after end date");
        }
        
        if (unique) {
            return repository.getUniqueStats(start, end, uris);
        } else {
            return repository.getStats(start, end, uris);
        }
    }
}
