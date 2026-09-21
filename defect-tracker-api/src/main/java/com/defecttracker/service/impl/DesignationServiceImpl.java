package com.defecttracker.service.impl;

import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Designation;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.DesignationRepository;
import com.defecttracker.service.DesignationService;
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
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;

    @Override
    @Transactional
    public Designation createDesignation(Designation designation) {
        String name = designation.getDesignationName() != null && !designation.getDesignationName().trim().isEmpty()
                ? designation.getDesignationName().trim()
                : (designation.getName() != null ? designation.getName().trim() : null);

        if (name == null || name.isEmpty()) {
            throw new BadRequestException("Designation name cannot be empty");
        }

        designation.setDesignationName(name);

        if (designationRepository.existsByDesignationName(designation.getDesignationName())) {
            throw new BadRequestException("Designation already exists: " + designation.getDesignationName());
        }
        return designationRepository.save(designation);
    }

    @Override
    @Transactional
    public Designation updateDesignation(Long id, Designation request) {
        Designation designation = getDesignationById(id);
        String name = request.getDesignationName() != null && !request.getDesignationName().trim().isEmpty()
                ? request.getDesignationName().trim()
                : (request.getName() != null ? request.getName().trim() : null);

        if (name != null && !name.isEmpty()) {
            designation.setDesignationName(name);
        }
        if (request.getDescription() != null) {
            designation.setDescription(request.getDescription());
        }
        return designationRepository.save(designation);
    }

    @Override
    @Transactional(readOnly = true)
    public Designation getDesignationById(Long id) {
        return designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Designation> getAllDesignations() {
        return designationRepository.findAll(Sort.by("id").ascending());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<Designation> getDesignationsPaginated(int page, int size) {
        Pageable pageable = PageableUtils.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());
        Page<Designation> p = designationRepository.findAll(pageable);
        return PaginatedResponse.<Designation>builder()
                .content(p.getContent())
                .pageNumber(p.getNumber())
                .pageSize(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .first(p.isFirst())
                .last(p.isLast())
                .build();
    }

    @Override
    @Transactional
    public void deleteDesignation(Long id) {
        Designation d = getDesignationById(id);
        designationRepository.delete(d);
    }
}
