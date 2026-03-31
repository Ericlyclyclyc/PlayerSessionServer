package org.lyc122.dev.playersessionserver.repository;

import org.lyc122.dev.playersessionserver.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUsername(String username);

    Optional<Player> findByUuid(String uuid);

    boolean existsByUsername(String username);

    boolean existsByUuid(String uuid);

    @Query(value = "SELECT id, current_server, last_online_time, offline_time, password, status, username, uuid FROM players WHERE username = :username", nativeQuery = true)
    Optional<Player> findByUsernameNative(@Param("username") String username);
}