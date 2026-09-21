package com.defecttracker.controller;

import com.defecttracker.dto.request.ClientRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.ClientSummary;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Client;
import com.defecttracker.mapper.ClientMapper;
import com.defecttracker.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client")
@RequiredArgsConstructor
@Tag(name = "Client Management", description = "Endpoints for managing clients according to ERD Client_Details")
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @PostMapping
    @PreAuthorize("@access.has('PROJECT_CREATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Create a new client")
    public ResponseEntity<ApiResponse<ClientSummary>> createClient(@Valid @RequestBody ClientRequest request) {
        Client client = clientService.createClient(request);
        return ResponseEntity.ok(ApiResponse.created(clientMapper.toSummary(client), "Client created successfully"));
    }

    @GetMapping
    @PreAuthorize("@access.has('PROJECT_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get clients with pagination or all list")
    public ResponseEntity<ApiResponse<PaginatedResponse<ClientSummary>>> getClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query
    ) {
        if (size >= 1000) {
            List<ClientSummary> clients = clientMapper.toSummaryList(clientService.getAllClients());
            PaginatedResponse<ClientSummary> res = PaginatedResponse.<ClientSummary>builder()
                    .content(clients).pageNumber(0).pageSize(clients.size())
                    .totalElements((long) clients.size()).totalPages(1).build();
            return ResponseEntity.ok(ApiResponse.success(res, "Clients retrieved"));
        }
        PaginatedResponse<Client> entityPage = clientService.searchClients(query, page, size);
        PaginatedResponse<ClientSummary> response = PaginatedResponse.<ClientSummary>builder()
                .content(clientMapper.toSummaryList(entityPage.getContent()))
                .pageNumber(entityPage.getPageNumber())
                .pageSize(entityPage.getPageSize())
                .totalElements(entityPage.getTotalElements())
                .totalPages(entityPage.getTotalPages())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response, "Clients retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.has('PROJECT_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ApiResponse<ClientSummary>> getClientById(@PathVariable Long id) {
        Client client = clientService.getClientById(id);
        return ResponseEntity.ok(ApiResponse.success(clientMapper.toSummary(client), "Client found"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@access.has('PROJECT_UPDATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Update client details")
    public ResponseEntity<ApiResponse<ClientSummary>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request
    ) {
        Client client = clientService.updateClient(id, request);
        return ResponseEntity.ok(ApiResponse.success(clientMapper.toSummary(client), "Client updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@access.has('PROJECT_DELETE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Delete client")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Client deleted successfully"));
    }
}
