package com.defecttracker.mapper;

import com.defecttracker.dto.response.UserSummary;
import com.defecttracker.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {DesignationMapper.class, RoleMapper.class})
public interface UserMapper {
    UserSummary toSummary(User user);
}
