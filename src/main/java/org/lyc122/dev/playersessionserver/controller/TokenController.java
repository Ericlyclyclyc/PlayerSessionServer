package org.lyc122.dev.playersessionserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyc122.dev.playersessionserver.dto.*;
import org.lyc122.dev.playersessionserver.entity.AccessToken;
import org.lyc122.dev.playersessionserver.entity.Player;
import org.lyc122.dev.playersessionserver.entity.TokenEvent;
import org.lyc122.dev.playersessionserver.service.PlayerService;
import org.lyc122.dev.playersessionserver.service.SessionService;
import org.lyc122.dev.playersessionserver.service.TokenService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final PlayerService playerService;
    private final TokenService tokenService;
    private final SessionService sessionService;

    /**
     * 获取AccessToken
     * POST /api/tokens/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<LoginResponse>> generate(@Valid @RequestBody GenerateTokenRequest request) {
        log.info("Generate token request for username: {} with uuid: {}", request.getUsername(), request.getUuid());

        // 验证玩家存在
        Player player = playerService.validatePlayer(request.getUsername(), request.getUuid());

        // 生成AccessToken
        AccessToken accessToken = tokenService.generateToken(player);

        // 构建响应
        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken.getToken())
                .username(player.getUsername())
                .uuid(player.getUuid())
                .expiresAt(accessToken.getCreatedAt().plusMinutes(3).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Token生成成功", response));
    }

    /**
     * 验证Token（玩家加入）
     * POST /api/tokens/validate
     * 需要API Key认证
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Void>> validate(@Valid @RequestBody ValidateTokenRequest request) {
        log.info("Token validation request for player: {}", request.getUsername());

        // 验证玩家存在（包括用户名和UUID匹配）
        Player player = playerService.validatePlayer(request.getUsername(), request.getUuid());

        // 验证Token
        AccessToken accessToken = tokenService.validateToken(request.getToken(), request.getServerName());

        // 检查Token是否属于该玩家
        if (!accessToken.getPlayer().getId().equals(player.getId())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(403, "Token不属于该玩家"));
        }

        // 创建或更新会话
        sessionService.createSession(player, request.getServerName());

        return ResponseEntity.ok(ApiResponse.success("Token验证成功", null));
    }

    /**
     * 使Token失效
     * POST /api/tokens/revoke
     * 用于安全措施，如检测到异常活动、强制下线等
     */
    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<Void>> revoke(@Valid @RequestBody RevokeTokenRequest request) {
        log.info("Token revoke request for player: {}", request.getUsername());

        // 验证玩家存在（包括用户名和UUID匹配）
        Player player = playerService.validatePlayer(request.getUsername(), request.getUuid());

        // 验证Token存在
        AccessToken accessToken = tokenService.getAccessTokenByToken(request.getToken());

        // 检查Token是否属于该玩家
        if (!accessToken.getPlayer().getId().equals(player.getId())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(403, "Token不属于该玩家"));
        }

        // 使Token失效（使用REVOKED事件类型）
        tokenService.revokeToken(request.getToken());

        // 如果玩家在线，结束其会话
        if (player.getStatus() == Player.PlayerStatus.ONLINE) {
            sessionService.endSession(player, player.getCurrentServer());
        }

        return ResponseEntity.ok(ApiResponse.success("Token已失效", null));
    }

    /**
     * 查询Token历史记录
     * GET /api/tokens/history?playerId={id}&limit=10&cursor=2026-03-30T10:00:00
     * <p>
     * 参数说明：
     * - playerId: 玩家ID（必填）
     * - limit: 返回的记录数量限制（默认10，最大100）
     * - cursor: 查询终点的时间戳（ISO格式），用于分页查询，不传则从最新记录开始
     * - startTime: 开始时间（可选）
     * - endTime: 结束时间（可选）
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TokenHistoryResponse>>> getTokenHistory(
            @RequestParam(required = false) Long playerId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        log.info("Get token history request for player ID: {}, limit: {}, cursor: {}", 
                 playerId, limit, cursor);

        // 验证playerId参数
        if (playerId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "playerId参数不能为空"));
        }

        // 限制最大数量
        if (limit > 100) {
            limit = 100;
        }

        // 根据ID查询玩家
        Player player = playerService.getAllPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Player not found"));

        LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
        LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;
        LocalDateTime cursorTime = cursor != null ? LocalDateTime.parse(cursor) : null;

        Pageable pageable = PageRequest.of(0, limit);
        Page<TokenEvent> events = tokenService.getTokenHistoryWithCursor(player, start, end, cursorTime, pageable);

        List<TokenHistoryResponse> responses = events.getContent().stream()
                .map(event -> TokenHistoryResponse.builder()
                        .id(event.getId())
                        .token(event.getAccessToken().getToken())
                        .eventType(event.getEventType().name())
                        .serverName(event.getServerName())
                        .eventTime(event.getEventTime())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}