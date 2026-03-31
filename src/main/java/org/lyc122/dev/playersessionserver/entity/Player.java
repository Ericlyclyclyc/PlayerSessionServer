package org.lyc122.dev.playersessionserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String username;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(nullable = false)
    private String password;

    @Column
    private LocalDateTime lastOnlineTime;

    @Column
    private LocalDateTime offlineTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status = PlayerStatus.OFFLINE;

    @Column(length = 100)
    private String currentServer;

    @PrePersist
    protected void onCreate() {
        status = PlayerStatus.OFFLINE;
    }

    public enum PlayerStatus {
        ONLINE,
        OFFLINE
    }
}