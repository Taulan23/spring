package ru.practicum.mainservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.mainservice.model.RequestStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    
    private Long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    
    private EventShortDto event;
    
    private UserShortDto requester;
    
    private RequestStatus status;
}
