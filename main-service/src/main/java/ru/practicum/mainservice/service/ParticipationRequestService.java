package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.dto.UserShortDto;
import ru.practicum.mainservice.exception.CategoryNotFoundException;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.ParticipationRequestRepository;
import ru.practicum.mainservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestService {
    
    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CategoryNotFoundException("Пользователь не найден"));
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CategoryNotFoundException("Событие не найдено"));
        
        // Проверяем, что заявка еще не создана
        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new IllegalStateException("Заявка уже существует");
        }
        
        // Проверяем, что это не инициатор события
        if (event.getInitiator().getId().equals(userId)) {
            throw new IllegalStateException("Инициатор не может подать заявку на свое событие");
        }
        
        // Проверяем, что событие опубликовано
        if (event.getState() != EventState.PUBLISHED) {
            throw new IllegalStateException("Нельзя участвовать в неопубликованном событии");
        }
        
        // Проверяем лимит участников
        if (event.getParticipantLimit() > 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new IllegalStateException("Достигнут лимит участников");
        }
        
        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);
        // Если лимит участников равен 0 или модерация отключена, заявка автоматически подтверждается
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }
        
        ParticipationRequest savedRequest = requestRepository.save(request);
        
        // Если заявка автоматически подтверждена, увеличиваем счетчик
        if (savedRequest.getStatus() == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        
        return mapToDto(savedRequest);
    }
    
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new CategoryNotFoundException("Заявка не найдена"));
        
        if (!request.getRequester().getId().equals(userId)) {
            throw new RuntimeException("Нельзя отменить чужую заявку");
        }
        
        // Проверяем, что заявка не подтверждена
        if (request.getStatus() == RequestStatus.CONFIRMED) {
            throw new IllegalStateException("Нельзя отменить уже подтвержденную заявку");
        }
        
        RequestStatus oldStatus = request.getStatus();
        request.setStatus(RequestStatus.CANCELED);
        
        // Если заявка была подтверждена, уменьшаем счетчик
        if (oldStatus == RequestStatus.CONFIRMED) {
            Event event = request.getEvent();
            event.setConfirmedRequests(Math.max(0, event.getConfirmedRequests() - 1));
            eventRepository.save(event);
        }
        
        ParticipationRequest savedRequest = requestRepository.save(request);
        return mapToDto(savedRequest);
    }
    
    private ParticipationRequestDto mapToDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                new EventShortDto(
                        String.valueOf(request.getEvent().getId()),
                        request.getEvent().getAnnotation(),
                        new CategoryDto(String.valueOf(request.getEvent().getCategory().getId()), request.getEvent().getCategory().getName()),
                        request.getEvent().getConfirmedRequests(),
                        request.getEvent().getEventDate(),
                        new UserShortDto(String.valueOf(request.getEvent().getInitiator().getId()), request.getEvent().getInitiator().getName()),
                        request.getEvent().getPaid(),
                        request.getEvent().getTitle(),
                        request.getEvent().getViews()
                ),
                new UserShortDto(String.valueOf(request.getRequester().getId()), request.getRequester().getName()),
                request.getStatus()
        );
    }
}
