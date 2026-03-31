package org.lyc122.dev.playersessionserver.repository;

import org.lyc122.dev.playersessionserver.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {
    
    Optional<Server> findByApiKey(String apiKey);
    
    Optional<Server> findByServerName(String serverName);
    
    boolean existsByApiKey(String apiKey);
    
    boolean existsByServerName(String serverName);
}