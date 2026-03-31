package org.lyc122.dev.playersessionserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @Column(nullable = false, length = 100)
    private String serverName;
    
    @Column(nullable = false)
    private LocalDateTime loginAt;
    
    @Column
    private LocalDateTime logoutAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isOnline = true;
    
    @PrePersist
    protected void onCreate() {
        loginAt = LocalDateTime.now();
        isOnline = true;
    }
}