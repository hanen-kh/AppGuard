package com.sfm.projetPFE.Client;

import jakarta.persistence.Entity;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientDTO {
    private String name;

    private String contactEmail;

    private String description;

    private Set<Long> supervisorIds = new HashSet<>();

    private int supervisorCount; // <- nouveau champ

    public ClientDTO(String name, String contactEmail, String description, int supervisorCount) {
        this.name = name;
        this.contactEmail = contactEmail;
        this.description = description;
        this.supervisorCount = supervisorCount;
    }


}
