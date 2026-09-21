package com.defecttracker.mapper;

import com.defecttracker.dto.response.KlocMetricResponse;
import com.defecttracker.entity.KlocMetric;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProjectMapper.class, DefectMapper.class})
public interface KlocMetricMapper {
    KlocMetricResponse toResponse(KlocMetric klocMetric);
    List<KlocMetricResponse> toResponseList(List<KlocMetric> list);
}
