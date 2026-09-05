package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiResponseDto {
    private String answer;
    private String intent;
    private List<String> actionLinks;
    private List<String> suggestions;
    private Object contextData;
}
