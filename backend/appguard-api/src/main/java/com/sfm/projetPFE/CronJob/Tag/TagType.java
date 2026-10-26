package com.sfm.projetPFE.CronJob.Tag;

public enum TagType {
    ENVIRONMENT,  // dev, prod, staging
    CLIENT,       // nom du client
    TYPE,         // backup, maintenance, business
    PRIORITY,     // high, medium, low
    CUSTOM        // tags personnalisés
}
