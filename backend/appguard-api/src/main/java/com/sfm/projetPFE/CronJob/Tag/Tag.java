package com.sfm.projetPFE.CronJob.Tag;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Tag implements Serializable {

   @Serial
   private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id;

    private String name;


    @Enumerated(EnumType.STRING)
    private TagType type = TagType.CUSTOM;




}
