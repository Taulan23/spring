package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    
    List<ParticipationRequest> findByRequesterId(Long requesterId);
    
    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long requesterId, Long eventId);
    
    List<ParticipationRequest> findByEventId(Long eventId);
    
    List<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);
}
