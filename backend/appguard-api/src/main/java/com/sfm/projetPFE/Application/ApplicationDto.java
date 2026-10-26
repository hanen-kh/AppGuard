package com.sfm.projetPFE.Application;

import com.sfm.projetPFE.Server.ServerDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationDto {
    private Long id;
    private String name;
    private String description;
    private String indexElasticsearch;
    private LocalDateTime createdAt;
    private String clientName;
    private ServerDto server;




}
