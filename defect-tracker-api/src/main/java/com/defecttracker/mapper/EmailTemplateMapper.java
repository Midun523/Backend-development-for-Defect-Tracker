package com.defecttracker.mapper;

import com.defecttracker.dto.response.EmailTemplateResponse;
import com.defecttracker.entity.EmailTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailTemplateMapper {
    @Mapping(target = "isDefault", expression = "java(template.isDefault())")
    @Mapping(target = "isActive", expression = "java(template.isActive())")
    EmailTemplateResponse toResponse(EmailTemplate template);

    List<EmailTemplateResponse> toResponseList(List<EmailTemplate> templates);
}
