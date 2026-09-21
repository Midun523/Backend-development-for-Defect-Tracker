package com.defecttracker.mapper;

import com.defecttracker.dto.response.ClientSummary;
import com.defecttracker.entity.Client;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    ClientSummary toSummary(Client client);
    List<ClientSummary> toSummaryList(List<Client> clients);
}
