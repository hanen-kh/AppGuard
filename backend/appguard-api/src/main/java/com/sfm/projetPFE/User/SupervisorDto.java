package com.sfm.projetPFE.User;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class SupervisorDto {

    private Long id;
    private String firstName;
    private String lastName;
    private long clientCount;
    public SupervisorDto(Long id, String firstName, String lastName, long clientCount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.clientCount = clientCount;
    }
}
