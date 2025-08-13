package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.dto.NewEventDto;
import ru.practicum.mainservice.dto.UpdateEventUserRequest;
import ru.practicum.mainservice.service.EventService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    
    private final EventService eventService;
    
    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(@PathVariable Long userId,
                                                   @RequestBody @Valid NewEventDto newEventDto) {
        EventFullDto event = eventService.createEvent(userId, newEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }
    
    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEventsByUser(@PathVariable Long userId,
                                                              @RequestParam(defaultValue = "0") int from,
                                                              @RequestParam(defaultValue = "10") int size) {
        List<EventShortDto> events = eventService.getEventsByUser(userId, from, size);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventByUser(@PathVariable Long userId,
                                                      @PathVariable Long eventId) {
        EventFullDto event = eventService.getEventByUser(userId, eventId);
        return ResponseEntity.ok(event);
    }
    
    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventByUser(@PathVariable Long userId,
                                                         @PathVariable Long eventId,
                                                         @RequestBody @Valid UpdateEventUserRequest updateRequest) {
        EventFullDto event = eventService.updateEventByUser(userId, eventId, updateRequest);
        return ResponseEntity.ok(event);
    }
}
