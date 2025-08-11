package ru.practicum.statsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.statsservice.dto.ViewStatsDto;
import ru.practicum.statsservice.model.EndpointHit;
import ru.practicum.statsservice.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    
    private final EndpointHitRepository repository;
    
    public EndpointHit saveHit(EndpointHit hit) {
        return repository.save(hit);
    }
    
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (unique) {
            return repository.getStatsUnique(start, end, uris);
        } else {
            return repository.getStats(start, end, uris);
        }
    }
}
