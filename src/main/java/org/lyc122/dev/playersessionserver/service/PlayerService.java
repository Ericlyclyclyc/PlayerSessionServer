package org.lyc122.dev.playersessionserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyc122.dev.playersessionserver.dto.PlayerResponse;
import org.lyc122.dev.playersessionserver.entity.Player;
import org.lyc122.dev.playersessionserver.exception.InvalidCredentialsException;
import org.lyc122.dev.playersessionserver.exception.PlayerAlreadyExistsException;
import org.lyc122.dev.playersessionserver.exception.PlayerNotFoundException;
import org.lyc122.dev.playersessionserver.repository.PlayerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 玩家注册
     */
    @Transactional
    public Player register(String username, String uuid, String password) {
        log.info("Registering player: {}", username);

        // 检查用户名是否已存在
        if (playerRepository.existsByUsername(username)) {
            throw new PlayerAlreadyExistsException("用户名已存在: " + username);
        }

        // 检查UUID是否已存在
        if (playerRepository.existsByUuid(uuid)) {
            throw new PlayerAlreadyExistsException("UUID已存在: " + uuid);
        }

        // 创建新玩家
        Player player = Player.builder()
                .username(username)
                .uuid(uuid)
                .password(passwordEncoder.encode(password))
                .status(Player.PlayerStatus.OFFLINE)
                .build();

        player = playerRepository.save(player);
        log.info("Player registered successfully: {}", username);
        return player;
    }

    /**
     * 玩家登录验证
     */
    public Player authenticate(String identifier, String password) {
        log.info("Authenticating player: {}", identifier);

        // 尝试通过用户名或UUID查找玩家
        Player player = playerRepository.findByUsername(identifier)
                .orElse(playerRepository.findByUuid(identifier)
                        .orElseThrow(() -> new InvalidCredentialsException("用户名或密码错误")));

        // 验证密码
        if (!passwordEncoder.matches(password, player.getPassword())) {
            throw new InvalidCredentialsException("用户名或密码错误");
        }

        log.info("Player authenticated successfully: {}", identifier);
        return player;
    }

    /**
     * 根据用户名或UUID查询玩家
     */
    public Player getPlayerByIdentifier(String identifier) {
        Player player = playerRepository.findByUsername(identifier).orElse(null);
        if (player == null) {
            player = playerRepository.findByUuid(identifier).orElse(null);
        }
        if (player == null) {
            throw new PlayerNotFoundException("玩家不存在: " + identifier);
        }
        return player;
    }

    /**
     * 根据用户名查询玩家
     */
    public Player getPlayerByUsername(String username) {
        return playerRepository.findByUsername(username)
                .orElseThrow(() -> new PlayerNotFoundException("玩家不存在: " + username));
    }

    /**
     * 根据UUID查询玩家
     */
    public Player getPlayerByUuid(String uuid) {
        return playerRepository.findByUuid(uuid)
                .orElseThrow(() -> new PlayerNotFoundException("玩家不存在: " + uuid));
    }

    /**
     * 根据用户名和UUID验证玩家存在
     */
    public Player validatePlayer(String username, String uuid) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new PlayerNotFoundException("玩家不存在: " + username));

        if (!player.getUuid().equals(uuid)) {
            throw new PlayerNotFoundException("UUID与玩家名不匹配");
        }

        return player;
    }

    /**
     * 获取玩家信息（响应DTO）
     */
    public PlayerResponse getPlayerResponse(String identifier) {
        Player player = getPlayerByIdentifier(identifier);
        return PlayerResponse.builder()
                .id(player.getId())
                .username(player.getUsername())
                .uuid(player.getUuid())
                .status(player.getStatus().name())
                .lastOnlineTime(player.getLastOnlineTime())
                .offlineTime(player.getOfflineTime())
                .currentServer(player.getCurrentServer())
                .build();
    }

    /**
     * 更新玩家在线状态
     */
    @Transactional
    public void updateOnlineStatus(Player player, boolean isOnline, String serverName) {
        if (isOnline) {
            player.setStatus(Player.PlayerStatus.ONLINE);
            player.setLastOnlineTime(java.time.LocalDateTime.now());
            player.setCurrentServer(serverName);
        } else {
            player.setStatus(Player.PlayerStatus.OFFLINE);
            player.setOfflineTime(java.time.LocalDateTime.now());
            player.setCurrentServer(null);
        }
        playerRepository.save(player);
        log.info("Player online status updated: {} isOnline={}", player.getUsername(), isOnline);
    }

    /**
     * 获取所有玩家
     */
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
}