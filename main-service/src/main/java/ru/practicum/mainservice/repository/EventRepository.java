package ru.practicum.mainservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);
    
    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);
    
    @Query("SELECT e FROM Event e " +
           "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
           "AND (:states IS NULL OR e.state IN :states) " +
           "AND (:categories IS NULL OR e.category.id IN :categories) " +
           "AND (cast(:rangeStart as timestamp) IS NULL OR e.eventDate >= :rangeStart) " +
           "AND (cast(:rangeEnd as timestamp) IS NULL OR e.eventDate <= :rangeEnd)")
    Page<Event> findEventsWithFilters(@Param("users") List<Long> users,
                                     @Param("states") List<EventState> states,
                                     @Param("categories") List<Long> categories,
                                     @Param("rangeStart") LocalDateTime rangeStart,
                                     @Param("rangeEnd") LocalDateTime rangeEnd,
                                     Pageable pageable);
    
    Page<Event> findByStateOrderByEventDateAsc(EventState state, Pageable pageable);
    
    Optional<Event> findByIdAndState(Long eventId, EventState state);
}
