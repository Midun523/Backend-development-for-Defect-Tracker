package com.defecttracker.service;

import com.defecttracker.dto.request.ClientRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Client;

import java.util.List;

public interface ClientService {
    Client createClient(ClientRequest request);
    Client updateClient(Long id, ClientRequest request);
    Client getClientById(Long id);
    List<Client> getAllClients();
    PaginatedResponse<Client> searchClients(String query, int page, int size);
    void deleteClient(Long id);
}
