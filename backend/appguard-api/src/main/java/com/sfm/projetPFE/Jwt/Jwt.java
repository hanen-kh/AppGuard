package com.sfm.projetPFE.Jwt;


import com.sfm.projetPFE.User.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Jwt implements Serializable {

    private static final long SerialVersionUID=1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private  boolean expired=false;
    private boolean desactivated=false;
    private String value;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private User user;


}