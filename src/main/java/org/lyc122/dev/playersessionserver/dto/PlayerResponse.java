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
public class PlayerResponse {

    private Long id;
    private String username;
    private String uuid;
    private String status;
    private LocalDateTime lastOnlineTime;
    private LocalDateTime offlineTime;
    private String currentServer;
}