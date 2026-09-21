package com.defecttracker.mapper;

import com.defecttracker.dto.response.*;
import com.defecttracker.entity.*;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EmployeeMapper.class, RoleMapper.class})
public interface PrivilegeMapper {

    PrivilegeTemplateResponse toTemplateResponse(PrivilegeTemplate template);
    List<PrivilegeTemplateResponse> toTemplateResponseList(List<PrivilegeTemplate> templates);

    UserPrivilegePreferenceResponse toUserPreferenceResponse(UserPrivilegePreference preference);
    List<UserPrivilegePreferenceResponse> toUserPreferenceResponseList(List<UserPrivilegePreference> preferences);

    RolePrivilegePreferenceResponse toRolePreferenceResponse(RolePrivilegePreference preference);
    List<RolePrivilegePreferenceResponse> toRolePreferenceResponseList(List<RolePrivilegePreference> preferences);
}
