package ru.practicum.mainservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @GetMapping("/")
    public String home() {
        return "Explore With Me - Main Service is running!";
    }
    
    @GetMapping("/test")
    public String test() {
        return "Test endpoint is working!";
    }
}
