package ru.practicum.statsservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_hits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "app", nullable = false)
    @NotBlank(message = "App не может быть пустым")
    private String app;
    
    @Column(name = "uri", nullable = false)
    @NotBlank(message = "Uri не может быть пустым")
    private String uri;
    
    @Column(name = "ip", nullable = false)
    @NotBlank(message = "Ip не может быть пустым")
    private String ip;
    
    @Column(name = "timestamp", nullable = false)
    @NotNull(message = "Timestamp не может быть null")
    private LocalDateTime timestamp;
}
