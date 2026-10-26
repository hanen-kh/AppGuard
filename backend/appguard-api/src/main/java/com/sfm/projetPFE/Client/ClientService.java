package com.sfm.projetPFE.Client;


import java.util.List;


public interface ClientService {

    List<ClientDTO> getClientsOfCurrentUser();

    void createClient(ClientDTO client);
    void updateClient(Long id, ClientDTO clientDetails);
    void deleteClient(Long id);
    Client getClientById(Long id);

}
