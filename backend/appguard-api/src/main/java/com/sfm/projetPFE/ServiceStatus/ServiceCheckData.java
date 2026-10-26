package com.sfm.projetPFE.ServiceStatus;

import lombok.*;

@Getter
@NoArgsConstructor

@Setter
@Builder
@AllArgsConstructor

public class ServiceCheckData {
    private String serviceName;
    private String hostIp;
    private String hostName;
    private String os;
    private String clientName;


}

