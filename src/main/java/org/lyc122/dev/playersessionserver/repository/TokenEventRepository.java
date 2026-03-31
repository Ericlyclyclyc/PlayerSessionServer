package org.lyc122.dev.playersessionserver.repository;

import org.lyc122.dev.playersessionserver.entity.AccessToken;
import org.lyc122.dev.playersessionserver.entity.Player;
import org.lyc122.dev.playersessionserver.entity.TokenEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TokenEventRepository extends JpaRepository<TokenEvent, Long> {
    
    Page<TokenEvent> findByAccessTokenPlayerAndEventTimeBetweenOrderByEventTimeDesc(
            Player player, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    Page<TokenEvent> findByAccessTokenPlayerOrderByEventTimeDesc(Player player, Pageable pageable);
    
    @Query("SELECT te FROM TokenEvent te WHERE te.accessToken.player = :player " +
           "AND te.eventTime BETWEEN :startTime AND :endTime " +
           "ORDER BY te.eventTime DESC")
    Page<TokenEvent> findByPlayerAndTimeRange(
            @Param("player") Player player,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    
    /**
     * 使用游标查询Token历史记录
     * 如果提供了cursorTime，则查询该时间之前的记录
     * 否则查询所有记录
     */
    @Query("SELECT te FROM TokenEvent te WHERE te.accessToken.player = :player " +
           "AND (:cursorTime IS NULL OR te.eventTime < :cursorTime) " +
           "AND (:startTime IS NULL OR te.eventTime >= :startTime) " +
           "AND (:endTime IS NULL OR te.eventTime <= :endTime) " +
           "ORDER BY te.eventTime DESC")
    Page<TokenEvent> findByPlayerWithCursor(
            @Param("player") Player player,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
}