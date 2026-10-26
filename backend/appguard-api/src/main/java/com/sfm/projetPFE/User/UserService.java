package com.sfm.projetPFE.User;

import com.sfm.projetPFE.Role.TypeRole;

import java.util.List;

public interface UserService {
    User createUser(UserDTO user);

    User getUserById(int id);



     List<UserDTO> getAllUsers();



    User updateUser(int id, User user);

    void deleteUser(int id);


    User loadUserByUsername(String username);
     UserDTO convertToDTO(User user);

    List<SupervisorDto> getAllSupervisors();

}
