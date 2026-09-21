package com.defecttracker.mapper;

import com.defecttracker.dto.response.DefectSummary;
import com.defecttracker.entity.Defect;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DefectMapper {
    DefectSummary toSummary(Defect defect);
}
