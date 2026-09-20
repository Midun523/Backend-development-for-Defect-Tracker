package com.defecttracker.controller;

import com.defecttracker.dto.request.ClientRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Client;
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

    @PostMapping
    @PreAuthorize("@access.has('PROJECT_CREATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Create a new client")
    public ResponseEntity<ApiResponse<Client>> createClient(@Valid @RequestBody ClientRequest request) {
        Client client = clientService.createClient(request);
        return ResponseEntity.ok(ApiResponse.created(client, "Client created successfully"));
    }

    @GetMapping
    @PreAuthorize("@access.has('PROJECT_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get clients with pagination or all list")
    public ResponseEntity<ApiResponse<Object>> getClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query
    ) {
        if (size >= 1000) {
            List<Client> clients = clientService.getAllClients();
            return ResponseEntity.ok(ApiResponse.success(clients, "Clients retrieved"));
        }
        PaginatedResponse<Client> response = clientService.searchClients(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Clients retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.has('PROJECT_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ApiResponse<Client>> getClientById(@PathVariable Long id) {
        Client client = clientService.getClientById(id);
        return ResponseEntity.ok(ApiResponse.success(client, "Client found"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@access.has('PROJECT_UPDATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Update client details")
    public ResponseEntity<ApiResponse<Client>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request
    ) {
        Client client = clientService.updateClient(id, request);
        return ResponseEntity.ok(ApiResponse.success(client, "Client updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@access.has('PROJECT_DELETE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Delete client")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Client deleted successfully"));
    }
}
