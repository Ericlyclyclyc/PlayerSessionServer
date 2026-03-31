package org.lyc122.dev.playersessionserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "玩家名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{1,16}$", message = "玩家名只能包含字母、数字和下划线，长度1-16")
    private String username;
    
    @NotBlank(message = "UUID不能为空")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
             message = "UUID格式不正确")
    private String uuid;
    
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,32}$", message = "密码长度必须在6-32之间")
    private String password;
}