package org.lyc122.dev.playersessionserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyc122.dev.playersessionserver.dto.ApiResponse;
import org.lyc122.dev.playersessionserver.dto.LogoutRequest;
import org.lyc122.dev.playersessionserver.dto.PlayerResponse;
import org.lyc122.dev.playersessionserver.dto.RegisterRequest;
import org.lyc122.dev.playersessionserver.entity.Player;
import org.lyc122.dev.playersessionserver.exception.PlayerNotFoundException;
import org.lyc122.dev.playersessionserver.repository.PlayerRepository;
import org.lyc122.dev.playersessionserver.service.PlayerService;
import org.lyc122.dev.playersessionserver.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Slf4j
public class PlayerController {

    private final PlayerService playerService;
    private final SessionService sessionService;
    private final PlayerRepository playerRepository;

    /**
     * 玩家注册
     * POST /api/players/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for username: {}", request.getUsername());

        Player player = playerService.register(
                request.getUsername(),
                request.getUuid(),
                request.getPassword()
        );

        return ResponseEntity.ok(ApiResponse.success("注册成功", null));
    }

    /**
     * 查询玩家信息
     * GET /api/players/{identifier}
     */
    @GetMapping("/{identifier}")
    public ResponseEntity<ApiResponse<PlayerResponse>> getPlayer(@PathVariable String identifier) {
        log.info("Get player info request for identifier: {}", identifier);

        // 使用分开的逻辑而不是链式Optional调用
        Player player = playerRepository.findByUsername(identifier)
                .orElse(null);

        if (player == null) {
            player = playerRepository.findByUuid(identifier)
                    .orElse(null);
        }

        if (player == null) {
            throw new PlayerNotFoundException("玩家不存在: " + identifier);
        }

        PlayerResponse response = PlayerResponse.builder()
                .id(player.getId())
                .username(player.getUsername())
                .uuid(player.getUuid())
                .status(player.getStatus().name())
                .lastOnlineTime(player.getLastOnlineTime())
                .offlineTime(player.getOfflineTime())
                .currentServer(player.getCurrentServer())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 玩家退出
     * POST /api/players/logout
     * 需要API Key认证
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        log.info("Logout request for username: {}", request.getUsername());

        // 根据用户名和UUID验证玩家
        Player player = playerService.validatePlayer(request.getUsername(), request.getUuid());

        // 结束会话
        sessionService.endSession(player, request.getServerName());

        return ResponseEntity.ok(ApiResponse.success("退出成功", null));
    }
}