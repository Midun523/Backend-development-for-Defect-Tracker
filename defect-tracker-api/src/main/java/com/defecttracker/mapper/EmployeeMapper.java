package com.defecttracker.mapper;

import com.defecttracker.dto.response.EmployeeResponse;
import com.defecttracker.entity.Employee;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, DesignationMapper.class})
public interface EmployeeMapper {
    EmployeeResponse toResponse(Employee employee);
    List<EmployeeResponse> toResponseList(List<Employee> employees);
}
