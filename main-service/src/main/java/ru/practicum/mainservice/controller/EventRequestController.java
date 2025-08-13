package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.EventRequestStatusUpdateRequest;
import ru.practicum.mainservice.dto.EventRequestStatusUpdateResult;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.service.EventRequestService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class EventRequestController {
    
    private final EventRequestService eventRequestService;
    
    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getEventRequests(@PathVariable Long userId,
                                                                          @PathVariable Long eventId) {
        List<ParticipationRequestDto> requests = eventRequestService.getEventRequests(userId, eventId);
        return ResponseEntity.ok(requests);
    }
    
    @PatchMapping
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        EventRequestStatusUpdateResult result = eventRequestService.updateRequestStatus(userId, eventId, updateRequest);
        return ResponseEntity.ok(result);
    }
}
