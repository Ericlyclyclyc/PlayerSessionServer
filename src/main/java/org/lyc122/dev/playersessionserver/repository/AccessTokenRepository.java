package org.lyc122.dev.playersessionserver.repository;

import org.lyc122.dev.playersessionserver.entity.AccessToken;
import org.lyc122.dev.playersessionserver.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

    Optional<AccessToken> findByToken(String token);

    List<AccessToken> findByPlayerAndExpiredOrderByCreatedAtDesc(Player player, Boolean expired);

    List<AccessToken> findByPlayerOrderByCreatedAtDesc(Player player);
}