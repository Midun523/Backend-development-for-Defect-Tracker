package com.defecttracker.service.impl;

import com.defecttracker.dto.request.ClientRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Client;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.ClientRepository;
import com.defecttracker.service.ClientService;
import com.defecttracker.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public Client createClient(ClientRequest request) {
        Client client = Client.builder()
                .clientName(request.getClientName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .country(request.getCountry())
                .state(request.getState())
                .build();
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public Client updateClient(Long id, ClientRequest request) {
        Client client = getClientById(id);
        client.setClientName(request.getClientName());
        client.setPhoneNumber(request.getPhoneNumber());
        client.setEmail(request.getEmail());
        client.setCountry(request.getCountry());
        client.setState(request.getState());
        return clientRepository.save(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getAllClients() {
        return clientRepository.findAll(Sort.by("id").descending());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<Client> searchClients(String query, int page, int size) {
        Pageable pageable = PageableUtils.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());
        Page<Client> clientPage;
        if (query != null && !query.trim().isEmpty()) {
            clientPage = clientRepository.searchClients(query.trim(), pageable);
        } else {
            clientPage = clientRepository.findAll(pageable);
        }

        return PaginatedResponse.<Client>builder()
                .content(clientPage.getContent())
                .pageNumber(clientPage.getNumber())
                .pageSize(clientPage.getSize())
                .totalElements(clientPage.getTotalElements())
                .totalPages(clientPage.getTotalPages())
                .first(clientPage.isFirst())
                .last(clientPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        Client client = getClientById(id);
        clientRepository.delete(client);
    }
}
