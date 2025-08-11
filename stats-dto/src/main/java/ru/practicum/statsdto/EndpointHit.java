package ru.practicum.statsdto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHit {
    
    @JsonProperty("id")
    private Long id;
    
    @NotBlank(message = "App не может быть пустым")
    @JsonProperty("app")
    private String app;
    
    @NotBlank(message = "URI не может быть пустым")
    @JsonProperty("uri")
    private String uri;
    
    @NotBlank(message = "IP не может быть пустым")
    @JsonProperty("ip")
    private String ip;
    
    @NotNull(message = "Timestamp не может быть null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
