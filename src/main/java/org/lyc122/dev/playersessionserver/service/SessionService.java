package org.lyc122.dev.playersessionserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyc122.dev.playersessionserver.entity.Player;
import org.lyc122.dev.playersessionserver.entity.PlayerSession;
import org.lyc122.dev.playersessionserver.exception.PlayerConflictException;
import org.lyc122.dev.playersessionserver.repository.PlayerSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionService {

    private final PlayerSessionRepository playerSessionRepository;
    private final PlayerService playerService;

    /**
     * 创建玩家会话（玩家登录）
     */
    @Transactional
    public PlayerSession createSession(Player player, String serverName) {
        log.info("Creating session for player: {} on server: {}", player.getUsername(), serverName);

        // 检查玩家是否已经在其他服务器在线
        Optional<PlayerSession> existingSession = playerSessionRepository.findByPlayerAndIsOnline(player, true);
        if (existingSession.isPresent()) {
            PlayerSession session = existingSession.get();
            if (!session.getServerName().equals(serverName)) {
                throw new PlayerConflictException(String.format(
                        "玩家 %s 已经在服务器 %s 在线，无法在服务器 %s 登录",
                        player.getUsername(), session.getServerName(), serverName));
            }
            // 如果在同一服务器，则更新现有会话
            return existingSession.get();
        }

        // 创建新会话
        PlayerSession session = PlayerSession.builder()
                .player(player)
                .serverName(serverName)
                .isOnline(true)
                .build();

        session = playerSessionRepository.save(session);

        // 更新玩家在线状态
        playerService.updateOnlineStatus(player, true, serverName);

        log.info("Session created for player: {} on server: {}", player.getUsername(), serverName);
        return session;
    }

    /**
     * 结束玩家会话（玩家登出）
     */
    @Transactional
    public void endSession(Player player, String serverName) {
        log.info("Ending session for player: {} on server: {}", player.getUsername(), serverName);

        // 查找当前活跃会话
        Optional<PlayerSession> existingSession = playerSessionRepository.findByPlayerAndIsOnline(player, true);
        if (existingSession.isPresent()) {
            PlayerSession session = existingSession.get();

            // 验证服务器名称是否匹配
            if (!session.getServerName().equals(serverName)) {
                log.warn("Server name mismatch for player {}: expected {}, got {}",
                        player.getUsername(), session.getServerName(), serverName);
            }

            // 结束会话
            session.setLogoutAt(java.time.LocalDateTime.now());
            session.setIsOnline(false);
            playerSessionRepository.save(session);
        }

        // 更新玩家在线状态
        playerService.updateOnlineStatus(player, false, serverName);

        log.info("Session ended for player: {}", player.getUsername());
    }

    /**
     * 验证玩家冲突
     */
    public void checkPlayerConflict(Player player, String serverName) {
        // 检查数据库
        Optional<PlayerSession> existingSession = playerSessionRepository.findByPlayerAndIsOnline(player, true);
        if (existingSession.isPresent() && !existingSession.get().getServerName().equals(serverName)) {
            throw new PlayerConflictException(String.format(
                    "玩家 %s 已经在服务器 %s 在线，无法在服务器 %s 登录",
                    player.getUsername(), existingSession.get().getServerName(), serverName));
        }
    }

    /**
     * 获取玩家当前会话
     */
    public Optional<PlayerSession> getCurrentSession(Player player) {
        return playerSessionRepository.findByPlayerAndIsOnline(player, true);
    }

    /**
     * 获取玩家会话历史
     */
    public List<PlayerSession> getPlayerSessions(Player player) {
        return playerSessionRepository.findByPlayerOrderByLoginAtDesc(player);
    }

    /**
     * 获取服务器上的所有在线玩家
     */
    public List<PlayerSession> getOnlinePlayers(String serverName) {
        return playerSessionRepository.findByServerNameAndIsOnline(serverName, true);
    }

    /**
     * 强制结束玩家会话（管理员功能）
     */
    @Transactional
    public void forceEndSession(Player player) {
        log.info("Force ending session for player: {}", player.getUsername());

        Optional<PlayerSession> existingSession = playerSessionRepository.findByPlayerAndIsOnline(player, true);
        if (existingSession.isPresent()) {
            PlayerSession session = existingSession.get();
            session.setLogoutAt(java.time.LocalDateTime.now());
            session.setIsOnline(false);
            playerSessionRepository.save(session);
        }

        // 更新玩家在线状态
        playerService.updateOnlineStatus(player, false, "admin");

        log.info("Session force ended for player: {}", player.getUsername());
    }
}