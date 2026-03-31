package org.lyc122.dev.playersessionserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenRequest {
    
    @NotBlank(message = "玩家名不能为空")
    private String username;
    
    @NotBlank(message = "UUID不能为空")
    private String uuid;
    
    @NotBlank(message = "Token不能为空")
    private String token;
    
    @NotBlank(message = "服务器名称不能为空")
    private String serverName;
}