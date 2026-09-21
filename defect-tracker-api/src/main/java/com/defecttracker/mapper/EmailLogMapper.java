package com.defecttracker.mapper;

import com.defecttracker.dto.response.EmailLogResponse;
import com.defecttracker.entity.EmailLog;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailLogMapper {
    EmailLogResponse toResponse(EmailLog log);
    List<EmailLogResponse> toResponseList(List<EmailLog> logs);
}
