package org.lyc122.dev.playersessionserver.repository;

import org.lyc122.dev.playersessionserver.entity.Player;
import org.lyc122.dev.playersessionserver.entity.PlayerSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerSessionRepository extends JpaRepository<PlayerSession, Long> {
    
    Optional<PlayerSession> findByPlayerAndIsOnline(Player player, Boolean isOnline);
    
    List<PlayerSession> findByPlayerOrderByLoginAtDesc(Player player);
    
    List<PlayerSession> findByServerNameAndIsOnline(String serverName, Boolean isOnline);
}