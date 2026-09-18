package com.defecttracker.repository;

import com.defecttracker.entity.RoleNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleNotificationSettingRepository extends JpaRepository<RoleNotificationSetting, Long> {
    List<RoleNotificationSetting> findByRoleId(Long roleId);
    Optional<RoleNotificationSetting> findByRoleIdAndPointKey(Long roleId, String pointKey);
}
