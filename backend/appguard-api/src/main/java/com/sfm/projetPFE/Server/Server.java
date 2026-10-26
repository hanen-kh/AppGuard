package com.sfm.projetPFE.Server;

import com.sfm.projetPFE.Application.Application;
import com.sfm.projetPFE.Client.Client;
import com.sfm.projetPFE.CronJob.Cron.CronJob;
import com.sfm.projetPFE.ServiceStatus.ServiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Server implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String os;
    private String name;
    private String ip;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private List<ServiceStatus> services = new ArrayList<>();

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ServerStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CronJob> cronJobs = new ArrayList<>();

    private String agentUrl; // URL de l’API de l’agent (ex. : https://server:8080/api)
    private String agentApiKey; // Clé API pour authentifier les appels à l’agent
    private int agentPort;
}
