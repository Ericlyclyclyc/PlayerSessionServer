package org.lyc122.dev.playersessionserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "servers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Server {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String serverName;
    
    @Column(nullable = false, unique = true, length = 64)
    private String apiKey;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private String description;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}