package com.sfm.projetPFE.Alert;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewEntityAlert {
    private String type; // "APPLICATION" ou "SERVER"
    private String message;
}
