package com.ems.controller;

import com.ems.dto.AiQueryDto;
import com.ems.dto.AiResponseDto;
import com.ems.dto.ApiResponse;
import com.ems.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/query")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AiResponseDto>> askAssistant(
            @RequestBody AiQueryDto queryDto,
            Authentication authentication) {
        String email = authentication.getName();
        AiResponseDto response = aiAssistantService.processQuery(email, queryDto);
        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }
}
