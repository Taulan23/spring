package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.*;
import ru.practicum.mainservice.exception.CategoryNotFoundException;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.ParticipationRequestRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventRequestService {
    
    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CategoryNotFoundException("Событие не найдено"));
        
        if (!event.getInitiator().getId().equals(userId)) {
            throw new RuntimeException("Только инициатор может просматривать заявки");
        }
        
        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, 
                                                            EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CategoryNotFoundException("Событие не найдено"));
        
        if (!event.getInitiator().getId().equals(userId)) {
            throw new RuntimeException("Только инициатор может изменять статус заявок");
        }
        
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        
        for (Long requestId : updateRequest.getRequestIds()) {
            ParticipationRequest request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new CategoryNotFoundException("Заявка не найдена"));
            
            if (!request.getEvent().getId().equals(eventId)) {
                throw new RuntimeException("Заявка не принадлежит данному событию");
            }
            
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new IllegalStateException("Можно изменить статус только у заявок в статусе PENDING");
            }
            
            // Проверяем лимит участников перед подтверждением
            if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
                if (event.getParticipantLimit() > 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
                    throw new IllegalStateException("Достигнут лимит участников для события");
                }
            }
            
            request.setStatus(updateRequest.getStatus());
            ParticipationRequest savedRequest = requestRepository.save(request);
            
            if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
                confirmedRequests.add(mapToDto(savedRequest));
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            } else if (updateRequest.getStatus() == RequestStatus.REJECTED) {
                rejectedRequests.add(mapToDto(savedRequest));
            }
        }
        
        eventRepository.save(event);
        
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }
    
    private ParticipationRequestDto mapToDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus()
        );
    }
}
