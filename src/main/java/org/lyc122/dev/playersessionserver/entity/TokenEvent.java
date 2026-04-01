package org.lyc122.dev.playersessionserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "token_id", nullable = false)
    private AccessToken accessToken;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType eventType;
    
    @Column(length = 100)
    private String serverName;
    
    @Column(nullable = false)
    private LocalDateTime eventTime;
    
    @PrePersist
    protected void onCreate() {
        eventTime = LocalDateTime.now();
    }
    
    public enum TokenType {
        GENERATED,
        VALIDATED,
        EXPIRED,
        INVALIDATED,
        RENEWED,
        REVOKED
    }
}