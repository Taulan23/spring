package ru.practicum.statsservice.controller;

import lombok.RequiredArgsConstructor;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    
    private final StatsService statsService;
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Stats Service is running!");
    }
    
    @PostMapping("/hit")
    public ResponseEntity<Void> hit(@RequestBody ru.practicum.statsdto.EndpointHit hitDto) {
        if (hitDto == null) {
            throw new ValidationException("Invalid request body");
        }
        
        if (hitDto.getApp() == null || hitDto.getApp().trim().isEmpty()) {
            throw new ValidationException("App не может быть пустым");
        }
        
        if (hitDto.getUri() == null || hitDto.getUri().trim().isEmpty()) {
            throw new ValidationException("URI не может быть пустым");
        }
        
        if (hitDto.getIp() == null || hitDto.getIp().trim().isEmpty()) {
            throw new ValidationException("IP не может быть пустым");
        }

        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(hitDto.getApp());
        endpointHit.setUri(hitDto.getUri());
        endpointHit.setIp(hitDto.getIp());
        endpointHit.setTimestamp(hitDto.getTimestamp() != null ? hitDto.getTimestamp() : LocalDateTime.now());

        statsService.saveHit(endpointHit);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {

        if (start == null || end == null) {
            throw new ValidationException("Start and end parameters are required");
        }

        String decodedStart = URLDecoder.decode(start, StandardCharsets.UTF_8);
        String decodedEnd = URLDecoder.decode(end, StandardCharsets.UTF_8);

        LocalDateTime startDateTime = parseDate(decodedStart);
        LocalDateTime endDateTime = parseDate(decodedEnd);

        if (uris != null && !uris.isEmpty()) {
            uris = uris.stream()
                    .filter(uri -> uri != null && !uri.trim().isEmpty())
                    .map(uri -> URLDecoder.decode(uri, StandardCharsets.UTF_8))
                    .toList();
        }

        List<ViewStatsDto> stats = statsService.getStats(startDateTime, endDateTime, uris, unique);
        return ResponseEntity.ok(stats);
    }

    private LocalDateTime parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("Date value cannot be null or empty");
        }
        
        try {
            // Пробуем формат с пробелом (из тестов: "yyyy-MM-dd HH:mm:ss")
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e1) {
            try {
                // Пробуем ISO формат с T (из StatsClient: "yyyy-MM-dd'T'HH:mm:ss")
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } catch (DateTimeParseException e2) {
                try {
                    // Пробуем стандартный ISO формат
                    return LocalDateTime.parse(value);
                } catch (DateTimeParseException e3) {
                    throw new ValidationException("Invalid date format: " + value + 
                        ". Expected formats: 'yyyy-MM-dd HH:mm:ss' or 'yyyy-MM-dd'T'HH:mm:ss'");
                }
            }
        }
    }
    

}
