package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {
    
    private final ParticipationRequestService requestService;
    
    @PostMapping
    public ResponseEntity<ParticipationRequestDto> createRequest(@PathVariable Long userId,
                                                                @RequestParam Long eventId) {
        ParticipationRequestDto request = requestService.createRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }
    
    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        List<ParticipationRequestDto> requests = requestService.getUserRequests(userId);
        return ResponseEntity.ok(requests);
    }
    
    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(@PathVariable Long userId,
                                                                @PathVariable Long requestId) {
        ParticipationRequestDto request = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(request);
    }
}
