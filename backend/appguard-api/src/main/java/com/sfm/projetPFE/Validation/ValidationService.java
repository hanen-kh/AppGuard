package com.sfm.projetPFE.Validation;

import java.util.Map;

public interface ValidationService {

    void codeActivation(String username);
    void requestNewPassword(Map<String, String> parametres);

    void changePassword(Map<String, String> parametres);

    Map<String, String> validateCode(Map<String, String> parametres);

}
