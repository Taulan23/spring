package ru.practicum.mainservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Column(name = "confirmed_requests")
    private Long confirmedRequests = 0L;
    
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
    
    @Column(name = "description", length = 7000)
    private String description;
    
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;
    
    @Embedded
    private Location location;
    
    @Column(name = "paid")
    private Boolean paid = false;
    
    @Column(name = "participant_limit")
    private Integer participantLimit = 0;
    
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    
    @Column(name = "request_moderation")
    private Boolean requestModeration = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private EventState state = EventState.PENDING;
    
    @Column(name = "title", nullable = false, length = 120)
    private String title;
    
    @Column(name = "views")
    private Long views = 0L;
}
