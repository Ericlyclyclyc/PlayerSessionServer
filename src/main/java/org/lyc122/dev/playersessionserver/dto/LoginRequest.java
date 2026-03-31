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
public class LoginRequest {
    
    @NotBlank(message = "玩家名或UUID不能为空")
    private String identifier;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}