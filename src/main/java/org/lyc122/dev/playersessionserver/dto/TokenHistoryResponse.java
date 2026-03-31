package org.lyc122.dev.playersessionserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenHistoryResponse {
    
    private Long id;
    private String token;
    private String eventType;
    private String serverName;
    private LocalDateTime eventTime;
}