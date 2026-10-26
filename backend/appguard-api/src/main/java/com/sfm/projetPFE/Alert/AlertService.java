package com.sfm.projetPFE.Alert;

import com.sfm.projetPFE.Server.Server;

public interface AlertService {
    Alert triggerAlert(String type, String message);

    //void notifyAdminsAndSupervisors(Server server, Alert alert);
}
