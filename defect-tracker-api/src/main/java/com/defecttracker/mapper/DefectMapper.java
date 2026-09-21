package com.defecttracker.mapper;

import com.defecttracker.dto.response.*;
import com.defecttracker.entity.Defect;
import com.defecttracker.entity.DefectComment;
import com.defecttracker.entity.DefectHistory;
import com.defecttracker.entity.DefectStatusLog;
import com.defecttracker.entity.DefectType;
import com.defecttracker.entity.StatusType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProjectMapper.class, ReleaseMapper.class, EmployeeMapper.class, UserMapper.class})
public interface DefectMapper {

    DefectSummary toSummary(Defect defect);

    DefectResponse toResponse(Defect defect);

    List<DefectResponse> toResponseList(List<Defect> defects);

    @Mapping(target = "isDefault", source = "default")
    @Mapping(target = "defectStatusName", source = "name")
    @Mapping(target = "statusName", source = "name")
    @Mapping(target = "statusType", source = "type")
    @Mapping(target = "colorCode", source = "color")
    DefectStatusSummary toStatusSummary(StatusType statusType);

    @Mapping(target = "defectTypeName", expression = "java(defectType != null ? (defectType.getDefectTypeName() != null ? defectType.getDefectTypeName() : defectType.getName()) : null)")
    DefectTypeSummary toTypeSummary(DefectType defectType);

    @Mapping(target = "leaderId", source = "leader.id")
    @Mapping(target = "moduleName", source = "name")
    @Mapping(target = "projectId", source = "project.id")
    ModuleSummary toModuleSummary(com.defecttracker.entity.Module module);

    DefectCommentResponse toCommentResponse(DefectComment comment);
    List<DefectCommentResponse> toCommentResponseList(List<DefectComment> comments);

    DefectHistoryResponse toHistoryResponse(DefectHistory history);
    List<DefectHistoryResponse> toHistoryResponseList(List<DefectHistory> histories);

    DefectStatusLogResponse toStatusLogResponse(DefectStatusLog log);
    List<DefectStatusLogResponse> toStatusLogResponseList(List<DefectStatusLog> logs);
}
