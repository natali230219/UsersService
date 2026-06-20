package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.EmailRequestDto;
import org.example.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-created")
    public ResponseEntity<Map<String, String>> sendCreatedEmail(@Valid @RequestBody EmailRequestDto requestDto) {
        emailService.sendAccountCreatedEmail(requestDto.getEmail(), requestDto.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email отправлен (имитация)");
        response.put("to", requestDto.getEmail());
        response.put("type", "created");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-deleted")
    public ResponseEntity<Map<String, String>> sendDeletedEmail(@Valid @RequestBody EmailRequestDto requestDto) {
        emailService.sendAccountDeletedEmail(requestDto.getEmail(), requestDto.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email отправлен (имитация)");
        response.put("to", requestDto.getEmail());
        response.put("type", "deleted");
        return ResponseEntity.ok(response);
    }

}
