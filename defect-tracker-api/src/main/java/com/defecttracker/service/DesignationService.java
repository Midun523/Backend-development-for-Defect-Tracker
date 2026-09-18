package com.defecttracker.service;

import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Designation;

import java.util.List;

public interface DesignationService {
    Designation createDesignation(Designation designation);
    Designation updateDesignation(Long id, Designation designation);
    Designation getDesignationById(Long id);
    List<Designation> getAllDesignations();
    PaginatedResponse<Designation> getDesignationsPaginated(int page, int size);
    void deleteDesignation(Long id);
}
