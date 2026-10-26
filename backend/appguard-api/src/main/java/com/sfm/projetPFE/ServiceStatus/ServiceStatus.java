package com.sfm.projetPFE.ServiceStatus;

import com.sfm.projetPFE.Server.Server;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String serviceName;  // MongoDB, MySQL, Tomcat, etc.

    @ManyToOne
    @JoinColumn(name = "server_id")
    private Server server;


    private Integer port;
    private Boolean isActive;
    private Double cpuUsage;     // en pourcentage
    private Double memoryUsage;  // en pourcentage
    private Double diskUsage;    // en pourcentage

    private LocalDateTime lastChecked;




}
