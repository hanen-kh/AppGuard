package com.sfm.projetPFE.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private int id;
    private String firstname;
    private String lastname;
    private String email;
    private String typeRole;
    private int roleId;

}
