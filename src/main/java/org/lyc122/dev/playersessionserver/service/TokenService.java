package org.lyc122.dev.playersessionserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyc122.dev.playersessionserver.entity.AccessToken;
import org.lyc122.dev.playersessionserver.entity.Player;
import org.lyc122.dev.playersessionserver.entity.TokenEvent;
import org.lyc122.dev.playersessionserver.exception.InvalidTokenException;
import org.lyc122.dev.playersessionserver.repository.AccessTokenRepository;
import org.lyc122.dev.playersessionserver.repository.TokenEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final AccessTokenRepository accessTokenRepository;
    private final TokenEventRepository tokenEventRepository;

    private static final int TOKEN_EXPIRE_MINUTES = 3;

    /**
     * 生成AccessToken
     */
    @Transactional
    public AccessToken generateToken(Player player) {
        log.info("Generating token for player: {}", player.getUsername());

        // 失效该玩家的所有旧token
        invalidateAllPlayerTokens(player);

        // 生成新的token
        String tokenString = UUID.randomUUID().toString();

        AccessToken accessToken = AccessToken.builder()
                .token(tokenString)
                .player(player)
                .createdAt(LocalDateTime.now())
                .expired(false)
                .lastUsedTime(LocalDateTime.now())
                .build();

        accessToken = accessTokenRepository.save(accessToken);

        // 记录token生成事件
        recordTokenEvent(accessToken, TokenEvent.TokenType.GENERATED, null);

        log.info("Token generated for player: {}", player.getUsername());
        return accessToken;
    }

    /**
     * 验证Token
     */
    @Transactional
    public AccessToken validateToken(String token, String serverName) {
        log.info("Validating token: {}", token);

        // 从数据库查询token
        AccessToken accessToken = accessTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token不存在"));

        // 检查token是否已过期
        if (accessToken.getExpired()) {
            throw new InvalidTokenException("Token已过期");
        }

        // 检查token是否超时（最后使用时间距现在超过3分钟）
        if (accessToken.getLastUsedTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            long minutesElapsed = java.time.Duration.between(accessToken.getLastUsedTime(), now).toMinutes();
            if (minutesElapsed >= TOKEN_EXPIRE_MINUTES) {
                // 标记token为过期
                invalidateToken(accessToken);
                throw new InvalidTokenException("Token已过期");
            }
        }

        // 验证通过，更新最后使用时间
        renewToken(accessToken, serverName);

        // 记录token验证事件
        recordTokenEvent(accessToken, TokenEvent.TokenType.VALIDATED, serverName);

        log.info("Token validated successfully: {}", token);
        return accessToken;
    }

    /**
     * 获取AccessToken（不验证有效性）
     */
    public AccessToken getAccessTokenByToken(String token) {
        return accessTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token不存在"));
    }

    /**
     * 续期Token（更新最后使用时间）
     */
    @Transactional
    public void renewToken(AccessToken accessToken, String serverName) {
        log.info("Renewing token: {}", accessToken.getToken());

        // 更新最后使用时间
        accessToken.setLastUsedTime(LocalDateTime.now());
        accessTokenRepository.save(accessToken);

        // 记录token续期事件
        recordTokenEvent(accessToken, TokenEvent.TokenType.RENEWED, serverName);

        log.info("Token renewed successfully: {}", accessToken.getToken());
    }

    /**
     * 玩家退出时记录Token使用时间（不立即过期）
     */
    @Transactional
    public void logoutToken(String token) {
        log.info("Logging out token: {}", token);

        AccessToken accessToken = accessTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token不存在"));

        // 更新最后使用时间，但不标记为过期
        // 玩家在3分钟内可以重新使用该token
        accessToken.setLastUsedTime(LocalDateTime.now());
        accessTokenRepository.save(accessToken);

        log.info("Token logged out (not expired yet): {}", token);
    }

    /**
     * 失效Token
     */
    @Transactional
    public void invalidateToken(AccessToken accessToken) {
        log.info("Invalidating token: {}", accessToken.getToken());

        accessToken.setExpired(true);
        accessTokenRepository.save(accessToken);

        // 记录token失效事件
        recordTokenEvent(accessToken, TokenEvent.TokenType.INVALIDATED, null);

        log.info("Token invalidated: {}", accessToken.getToken());
    }

    /**
     * 失效玩家的所有Token
     */
    @Transactional
    public void invalidateAllPlayerTokens(Player player) {
        log.info("Invalidating all tokens for player: {}", player.getUsername());

        List<AccessToken> activeTokens = accessTokenRepository.findByPlayerAndExpiredOrderByCreatedAtDesc(player, false);
        for (AccessToken token : activeTokens) {
            invalidateToken(token);
        }
    }

    /**
     * 失效指定Token
     */
    @Transactional
    public void invalidateToken(String token) {
        AccessToken accessToken = accessTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token不存在"));
        invalidateToken(accessToken);
    }

    /**
     * 使Token失效（手动调用，使用REVOKED事件）
     */
    @Transactional
    public void revokeToken(String token) {
        log.info("Revoking token: {}", token);

        AccessToken accessToken = accessTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token不存在"));

        accessToken.setExpired(true);
        accessTokenRepository.save(accessToken);

        // 记录token撤销事件
        recordTokenEvent(accessToken, TokenEvent.TokenType.REVOKED, null);

        log.info("Token revoked: {}", token);
    }

    /**
     * 记录Token事件
     */
    private void recordTokenEvent(AccessToken accessToken, TokenEvent.TokenType eventType, String serverName) {
        TokenEvent event = TokenEvent.builder()
                .accessToken(accessToken)
                .eventType(eventType)
                .serverName(serverName)
                .build();

        tokenEventRepository.save(event);
    }

    /**
     * 获取Token历史记录
     */
    public Page<TokenEvent> getTokenHistory(Player player, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (startTime != null && endTime != null) {
            return tokenEventRepository.findByPlayerAndTimeRange(player, startTime, endTime, pageable);
        } else {
            return tokenEventRepository.findByAccessTokenPlayerOrderByEventTimeDesc(player, pageable);
        }
    }

    /**
     * 使用游标获取Token历史记录
     * @param player 玩家
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param cursorTime 游标时间（查询终点，可选）
     * @param pageable 分页参数
     * @return Token事件分页结果
     */
    public Page<TokenEvent> getTokenHistoryWithCursor(Player player, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime cursorTime, Pageable pageable) {
        return tokenEventRepository.findByPlayerWithCursor(player, cursorTime, startTime, endTime, pageable);
    }

    /**
     * 清理过期的Token
     */
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired tokens");

        List<AccessToken> expiredTokens = accessTokenRepository.findAll().stream()
                .filter(token -> !token.getExpired() && token.getLastUsedTime() != null)
                .filter(token -> {
                    long minutesElapsed = java.time.Duration.between(token.getLastUsedTime(), LocalDateTime.now()).toMinutes();
                    return minutesElapsed >= TOKEN_EXPIRE_MINUTES;
                })
                .toList();

        for (AccessToken token : expiredTokens) {
            token.setExpired(true);
            accessTokenRepository.save(token);
            recordTokenEvent(token, TokenEvent.TokenType.EXPIRED, null);
        }

        log.info("Cleaned up {} expired tokens", expiredTokens.size());
    }
}