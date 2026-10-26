package com.sfm.projetPFE.Server;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServerDto {
    private Long id;
    private String name;
    private String os;
    private String ip;
    private List<String> services;
    private String Client;
    private int CronJobs;
    private int Applications;
    private int servicesNb;
    private ServerStatus status;
    // Liste des noms de services
    private List<String> serviceNames;

    // Liste des noms d’applications
    private List<String> applicationNames;
    public ServerDto(long id, String name, String os, String ip) {
    }




}
