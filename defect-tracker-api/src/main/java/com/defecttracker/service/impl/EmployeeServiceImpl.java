package com.defecttracker.service.impl;

import com.defecttracker.dto.request.EmployeeCreateRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Designation;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.Role;
import com.defecttracker.entity.User;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.DesignationRepository;
import com.defecttracker.repository.EmployeeRepository;
import com.defecttracker.repository.RoleRepository;
import com.defecttracker.repository.UserRepository;
import com.defecttracker.service.EmployeeService;
import com.defecttracker.service.ProjectSequenceService;
import com.defecttracker.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DesignationRepository designationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectSequenceService projectSequenceService;

    @Override
    @Transactional
    public Employee createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email address is already registered. Please use a different email.");
        }

        Designation designation = null;
        if (request.getDesignationId() != null) {
            designation = designationRepository.findById(request.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.getDesignationId()));
        }

        String userCode = projectSequenceService.getNextEmployeeCode();

        String rawPassword = (request.getPassword() != null && !request.getPassword().trim().isEmpty())
                ? request.getPassword()
                : "Pass@" + UUID.randomUUID().toString().substring(0, 6);

        Set<Role> roles = new HashSet<>();
        if (request.getRoleId() != null) {
            Role r = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
            roles.add(r);
        } else {
            Role defRole = roleRepository.findTopByOrderByIdAsc()
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "default", "none found"));
            roles.add(defRole);
        }

        User user = User.builder()
                .userId(userCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .phone(request.getPhone())
                .gender(request.getGender())
                .userStatus("ACTIVE")
                .userType("CompanyStaff")
                .designation(designation)
                .roles(roles)
                .build();
        user = userRepository.save(user);

        String skillsCsv = request.getSkills() != null ? String.join(",", request.getSkills()) : "";

        Employee employee = Employee.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .gender(request.getGender())
                .designation(designation)
                .experience(request.getExperience() != null ? request.getExperience() : 0.0)
                .joinedDate(request.getJoinedDate() != null ? request.getJoinedDate() : LocalDate.now())
                .skills(skillsCsv)
                .availability(request.getAvailability() != null ? request.getAvailability() : 100)
                .status(request.getStatus() != null ? request.getStatus() : "active")
                .department(request.getDepartment())
                .manager(request.getManager())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .address(request.getAddress())
                .build();

        Employee saved = employeeRepository.save(employee);
        if (designation != null) {
            designation.setTotalEmployees(designation.getTotalEmployees() + 1);
            designationRepository.save(designation);
        }
        return saved;
    }

    @Override
    @Transactional
    public Employee updateEmployee(Long id, EmployeeCreateRequest request) {
        Employee employee = getEmployeeById(id);

        if (request.getDesignationId() != null) {
            Designation d = designationRepository.findById(request.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.getDesignationId()));
            employee.setDesignation(d);
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        if (request.getGender() != null) employee.setGender(request.getGender());
        if (request.getExperience() != null) employee.setExperience(request.getExperience());
        if (request.getJoinedDate() != null) employee.setJoinedDate(request.getJoinedDate());
        if (request.getSkills() != null) employee.setSkills(String.join(",", request.getSkills()));
        if (request.getAvailability() != null) employee.setAvailability(request.getAvailability());
        if (request.getStatus() != null) employee.setStatus(request.getStatus());
        if (request.getDepartment() != null) employee.setDepartment(request.getDepartment());
        if (request.getManager() != null) employee.setManager(request.getManager());
        if (request.getStartDate() != null) employee.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) employee.setEndDate(request.getEndDate());
        if (request.getAddress() != null) employee.setAddress(request.getAddress());

        if (employee.getUser() != null) {
            User user = employee.getUser();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhone(request.getPhone());
            user.setDesignation(employee.getDesignation());
            userRepository.save(user);
        }

        return employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<Employee> getAllEmployees(int page, int size, String query) {
        Pageable pageable = PageableUtils.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());
        Page<Employee> employeePage;
        if (query != null && !query.trim().isEmpty()) {
            employeePage = employeeRepository.searchEmployees(query.trim(), pageable);
        } else {
            employeePage = employeeRepository.findAll(pageable);
        }

        return PaginatedResponse.<Employee>builder()
                .content(employeePage.getContent())
                .pageNumber(employeePage.getNumber())
                .pageSize(employeePage.getSize())
                .totalElements(employeePage.getTotalElements())
                .totalPages(employeePage.getTotalPages())
                .first(employeePage.isFirst())
                .last(employeePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployeesList() {
        return employeeRepository.findAll();
    }

    @Override
    @Transactional
    public Employee updateEmployeeStatus(Long id, String status) {
        Employee employee = getEmployeeById(id);
        employee.setStatus(status);
        if (employee.getUser() != null) {
            employee.getUser().setUserStatus(status.toUpperCase());
            userRepository.save(employee.getUser());
        }
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getBenchEmployees() {
        return employeeRepository.findBenchEmployees();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDesignation(Long designationId) {
        return employeeRepository.findByDesignationId(designationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAvailableManagers(Long designationId, Long projectId) {
        if (designationId != null) {
            return employeeRepository.findAvailableManagersByDesignation(designationId);
        }
        return employeeRepository.findByStatus("active");
    }
}
