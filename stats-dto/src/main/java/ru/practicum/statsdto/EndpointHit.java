package ru.practicum.statsdto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
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
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getApp() {
        return app;
    }
    
    public void setApp(String app) {
        this.app = app;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
}
