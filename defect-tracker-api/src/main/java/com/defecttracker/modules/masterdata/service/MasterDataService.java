package com.defecttracker.modules.masterdata.service;

import com.defecttracker.common.exception.BusinessRuleException;
import com.defecttracker.common.exception.ResourceNotFoundException;
import com.defecttracker.modules.access.entity.Role;
import com.defecttracker.modules.access.repository.RoleRepository;
import com.defecttracker.modules.masterdata.dto.MasterDataDto;
import com.defecttracker.modules.masterdata.entity.DefectType;
import com.defecttracker.modules.masterdata.entity.Designation;
import com.defecttracker.modules.masterdata.entity.Priority;
import com.defecttracker.modules.masterdata.entity.ReleaseType;
import com.defecttracker.modules.masterdata.entity.Severity;
import com.defecttracker.modules.masterdata.repository.DefectTypeRepository;
import com.defecttracker.modules.masterdata.repository.DesignationRepository;
import com.defecttracker.modules.masterdata.repository.PriorityRepository;
import com.defecttracker.modules.masterdata.repository.ReleaseTypeRepository;
import com.defecttracker.modules.masterdata.repository.SeverityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MasterDataService {

    private final SeverityRepository severityRepository;
    private final PriorityRepository priorityRepository;
    private final DefectTypeRepository defectTypeRepository;
    private final ReleaseTypeRepository releaseTypeRepository;
    private final DesignationRepository designationRepository;
    private final RoleRepository roleRepository;

    // --- Severities ---
    @Transactional(readOnly = true)
    public List<MasterDataDto.SeverityDto> getAllSeverities() {
        return severityRepository.findAllByOrderByWeightAsc().stream()
                .map(this::mapSeverity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MasterDataDto.SeverityDto createSeverity(MasterDataDto.SeverityDto dto) {
        if (severityRepository.existsByName(dto.getName())) {
            throw new BusinessRuleException("Severity with name '" + dto.getName() + "' already exists");
        }
        Severity s = Severity.builder()
                .name(dto.getName())
                .weight(dto.getWeight() != null ? dto.getWeight() : 1)
                .description(dto.getDescription())
                .colorCode(dto.getColorCode() != null ? dto.getColorCode() : "#3B82F6")
                .build();
        return mapSeverity(severityRepository.save(s));
    }

    @Transactional
    public MasterDataDto.SeverityDto updateSeverity(Long id, MasterDataDto.SeverityDto dto) {
        Severity s = severityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Severity", "id", id));
        s.setName(dto.getName());
        s.setWeight(dto.getWeight());
        s.setDescription(dto.getDescription());
        s.setColorCode(dto.getColorCode());
        return mapSeverity(severityRepository.save(s));
    }

    @Transactional
    public void deleteSeverity(Long id) {
        severityRepository.deleteById(id);
    }

    // --- Priorities ---
    @Transactional(readOnly = true)
    public List<MasterDataDto.PriorityDto> getAllPriorities() {
        return priorityRepository.findAllByOrderByWeightAsc().stream()
                .map(this::mapPriority)
                .collect(Collectors.toList());
    }

    @Transactional
    public MasterDataDto.PriorityDto createPriority(MasterDataDto.PriorityDto dto) {
        if (priorityRepository.existsByName(dto.getName())) {
            throw new BusinessRuleException("Priority with name '" + dto.getName() + "' already exists");
        }
        Priority p = Priority.builder()
                .name(dto.getName())
                .weight(dto.getWeight() != null ? dto.getWeight() : 1)
                .description(dto.getDescription())
                .colorCode(dto.getColorCode() != null ? dto.getColorCode() : "#3B82F6")
                .build();
        return mapPriority(priorityRepository.save(p));
    }

    @Transactional
    public MasterDataDto.PriorityDto updatePriority(Long id, MasterDataDto.PriorityDto dto) {
        Priority p = priorityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Priority", "id", id));
        p.setName(dto.getName());
        p.setWeight(dto.getWeight());
        p.setDescription(dto.getDescription());
        p.setColorCode(dto.getColorCode());
        return mapPriority(priorityRepository.save(p));
    }

    @Transactional
    public void deletePriority(Long id) {
        priorityRepository.deleteById(id);
    }

    // --- Defect Types ---
    @Transactional(readOnly = true)
    public List<MasterDataDto.DefectTypeDto> getAllDefectTypes() {
        return defectTypeRepository.findAll().stream()
                .map(this::mapDefectType)
                .collect(Collectors.toList());
    }

    @Transactional
    public MasterDataDto.DefectTypeDto createDefectType(MasterDataDto.DefectTypeDto dto) {
        if (defectTypeRepository.existsByName(dto.getName())) {
            throw new BusinessRuleException("Defect type with name '" + dto.getName() + "' already exists");
        }
        DefectType dt = DefectType.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .colorCode(dto.getColorCode() != null ? dto.getColorCode() : "#6B7280")
                .build();
        return mapDefectType(defectTypeRepository.save(dt));
    }

    @Transactional
    public MasterDataDto.DefectTypeDto updateDefectType(Long id, MasterDataDto.DefectTypeDto dto) {
        DefectType dt = defectTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DefectType", "id", id));
        dt.setName(dto.getName());
        dt.setDescription(dto.getDescription());
        dt.setColorCode(dto.getColorCode());
        return mapDefectType(defectTypeRepository.save(dt));
    }

    @Transactional
    public void deleteDefectType(Long id) {
        defectTypeRepository.deleteById(id);
    }

    // --- Release Types ---
    @Transactional(readOnly = true)
    public List<MasterDataDto.ReleaseTypeDto> getAllReleaseTypes() {
        return releaseTypeRepository.findAll().stream()
                .map(this::mapReleaseType)
                .collect(Collectors.toList());
    }

    @Transactional
    public MasterDataDto.ReleaseTypeDto createReleaseType(MasterDataDto.ReleaseTypeDto dto) {
        if (releaseTypeRepository.existsByName(dto.getName())) {
            throw new BusinessRuleException("Release type with name '" + dto.getName() + "' already exists");
        }
        ReleaseType rt = ReleaseType.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .colorCode(dto.getColorCode() != null ? dto.getColorCode() : "#6B7280")
                .build();
        return mapReleaseType(releaseTypeRepository.save(rt));
    }

    @Transactional
    public MasterDataDto.ReleaseTypeDto updateReleaseType(Long id, MasterDataDto.ReleaseTypeDto dto) {
        ReleaseType rt = releaseTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReleaseType", "id", id));
        rt.setName(dto.getName());
        rt.setDescription(dto.getDescription());
        rt.setColorCode(dto.getColorCode());
        return mapReleaseType(releaseTypeRepository.save(rt));
    }

    @Transactional
    public void deleteReleaseType(Long id) {
        releaseTypeRepository.deleteById(id);
    }

    // --- Designations ---
    @Transactional(readOnly = true)
    public List<MasterDataDto.DesignationDto> getAllDesignations() {
        return designationRepository.findAll().stream()
                .map(this::mapDesignation)
                .collect(Collectors.toList());
    }

    @Transactional
    public MasterDataDto.DesignationDto createDesignation(MasterDataDto.DesignationDto dto) {
        if (designationRepository.existsByName(dto.getName())) {
            throw new BusinessRuleException("Designation with name '" + dto.getName() + "' already exists");
        }
        Designation d = Designation.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .projectManagerEligible(dto.isProjectManagerEligible())
                .build();
        return mapDesignation(designationRepository.save(d));
    }

    @Transactional
    public MasterDataDto.DesignationDto updateDesignation(Long id, MasterDataDto.DesignationDto dto) {
        Designation d = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
        d.setName(dto.getName());
        d.setDescription(dto.getDescription());
        d.setProjectManagerEligible(dto.isProjectManagerEligible());
        return mapDesignation(designationRepository.save(d));
    }

    @Transactional
    public void deleteDesignation(Long id) {
        designationRepository.deleteById(id);
    }

    // --- Roles ---
    @Transactional(readOnly = true)
    public List<MasterDataDto.RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapRole)
                .collect(Collectors.toList());
    }

    // Mapping helpers
    private MasterDataDto.SeverityDto mapSeverity(Severity s) {
        return MasterDataDto.SeverityDto.builder()
                .id(s.getId()).name(s.getName()).weight(s.getWeight())
                .description(s.getDescription()).colorCode(s.getColorCode()).build();
    }

    private MasterDataDto.PriorityDto mapPriority(Priority p) {
        return MasterDataDto.PriorityDto.builder()
                .id(p.getId()).name(p.getName()).weight(p.getWeight())
                .description(p.getDescription()).colorCode(p.getColorCode()).build();
    }

    private MasterDataDto.DefectTypeDto mapDefectType(DefectType dt) {
        return MasterDataDto.DefectTypeDto.builder()
                .id(dt.getId()).name(dt.getName()).description(dt.getDescription())
                .colorCode(dt.getColorCode()).build();
    }

    private MasterDataDto.ReleaseTypeDto mapReleaseType(ReleaseType rt) {
        return MasterDataDto.ReleaseTypeDto.builder()
                .id(rt.getId()).name(rt.getName()).description(rt.getDescription())
                .colorCode(rt.getColorCode()).build();
    }

    private MasterDataDto.DesignationDto mapDesignation(Designation d) {
        return MasterDataDto.DesignationDto.builder()
                .id(d.getId()).name(d.getName()).description(d.getDescription())
                .projectManagerEligible(d.isProjectManagerEligible()).build();
    }

    private MasterDataDto.RoleDto mapRole(Role r) {
        return MasterDataDto.RoleDto.builder()
                .id(r.getId()).name(r.getName()).description(r.getDescription())
                .admin(r.isAdmin()).build();
    }
}
