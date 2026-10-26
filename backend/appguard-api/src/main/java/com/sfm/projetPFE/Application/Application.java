package com.sfm.projetPFE.Application;

import com.sfm.projetPFE.Server.Server;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Application implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private long id;

    private String name;
    private String indexElasticsearch;
    private String description;
    private LocalDateTime createdAt;
    private String clientName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private Server server;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
