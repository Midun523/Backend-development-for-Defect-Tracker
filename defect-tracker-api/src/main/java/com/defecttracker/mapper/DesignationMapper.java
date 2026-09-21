package com.defecttracker.mapper;

import com.defecttracker.dto.response.DesignationSummary;
import com.defecttracker.entity.Designation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DesignationMapper {
    @Mapping(target = "name", expression = "java(designation.getDesignationName())")
    DesignationSummary toSummary(Designation designation);
}
