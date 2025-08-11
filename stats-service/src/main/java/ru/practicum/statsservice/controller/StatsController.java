package ru.practicum.statsservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statsservice.dto.ViewStatsDto;
import ru.practicum.statsservice.exception.ValidationException;
import ru.practicum.statsservice.model.EndpointHit;
import ru.practicum.statsservice.service.StatsService;

import jakarta.validation.Valid;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    
    private final StatsService statsService;
    
    @PostMapping("/hit")
    public ResponseEntity<Void> hit(@Valid @RequestBody EndpointHit endpointHit) {
        if (endpointHit == null) {
            throw new ValidationException("Request body cannot be null");
        }
        statsService.saveHit(endpointHit);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {
        
        // Декодируем параметры если они закодированы
        if (uris != null && !uris.isEmpty()) {
            uris = uris.stream()
                    .filter(uri -> uri != null && !uri.trim().isEmpty())
                    .map(uri -> URLDecoder.decode(uri, StandardCharsets.UTF_8))
                    .toList();
        }
        
        List<ViewStatsDto> stats = statsService.getStats(start, end, uris, unique);
        return ResponseEntity.ok(stats);
    }
}
