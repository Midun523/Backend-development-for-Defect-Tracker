package com.defecttracker.mapper;

import com.defecttracker.dto.response.EmailConfigResponse;
import com.defecttracker.entity.EmailConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailConfigMapper {
    @Mapping(target = "passwordSet", expression = "java(config.getPassword() != null && !config.getPassword().trim().isEmpty())")
    @Mapping(target = "isDefault", expression = "java(config.isDefault())")
    @Mapping(target = "isActive", expression = "java(config.isActive())")
    EmailConfigResponse toResponse(EmailConfig config);

    List<EmailConfigResponse> toResponseList(List<EmailConfig> configs);
}
