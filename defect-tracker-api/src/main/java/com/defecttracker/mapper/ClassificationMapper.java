package com.defecttracker.mapper;

import com.defecttracker.dto.response.*;
import com.defecttracker.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClassificationMapper {

    // --- Priority ---
    PrioritySummary toPrioritySummary(Priority priority);
    List<PrioritySummary> toPrioritySummaryList(List<Priority> priorities);

    // --- Severity ---
    SeveritySummary toSeveritySummary(Severity severity);
    List<SeveritySummary> toSeveritySummaryList(List<Severity> severities);

    // --- DefectType ---
    @Mapping(target = "defectTypeName", expression = "java(defectType.getDefectTypeName() != null ? defectType.getDefectTypeName() : defectType.getName())")
    DefectTypeSummary toDefectTypeSummary(DefectType defectType);
    List<DefectTypeSummary> toDefectTypeSummaryList(List<DefectType> defectTypes);

    // --- ReleaseType ---
    ReleaseTypeSummary toReleaseTypeSummary(ReleaseType releaseType);
    List<ReleaseTypeSummary> toReleaseTypeSummaryList(List<ReleaseType> releaseTypes);

    // --- StatusType → DefectStatusSummary ---
    @Mapping(target = "defectStatusName", source = "name")
    @Mapping(target = "statusName", source = "name")
    @Mapping(target = "statusType", source = "type")
    @Mapping(target = "colorCode", source = "color")
    DefectStatusSummary toDefectStatusSummary(StatusType statusType);
    List<DefectStatusSummary> toDefectStatusSummaryList(List<StatusType> statusTypes);

    // --- StatusTransition ---
    @Mapping(target = "fromStatus", source = "fromStatus")
    @Mapping(target = "toStatus", source = "toStatus")
    StatusTransitionResponse toStatusTransitionResponse(StatusTransition transition);
    List<StatusTransitionResponse> toStatusTransitionResponseList(List<StatusTransition> transitions);

    // --- WorkflowPosition ---
    WorkflowPositionResponse toWorkflowPositionResponse(WorkflowPosition position);
    List<WorkflowPositionResponse> toWorkflowPositionResponseList(List<WorkflowPosition> positions);
}
