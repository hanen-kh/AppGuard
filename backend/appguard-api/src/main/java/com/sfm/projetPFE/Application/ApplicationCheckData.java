package com.sfm.projetPFE.Application;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
 public  class ApplicationCheckData {
    private String applicationName;
    private String hostIp;
    private String hostName;
    private String os;
    private String clientName;


}