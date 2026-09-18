package com.defecttracker.service;

import com.defecttracker.dto.request.EmailConfigDTO;
import com.defecttracker.dto.request.EmailTemplateDTO;
import com.defecttracker.dto.request.RoleNotificationUpdateDTO;
import com.defecttracker.dto.request.UserExtraRulesUpdateDTO;
import com.defecttracker.entity.EmailConfig;
import com.defecttracker.entity.EmailLog;
import com.defecttracker.entity.EmailTemplate;

import java.util.List;
import java.util.Map;

public interface EmailService {
    boolean sendEmail(String to, String subject, String body);
    boolean sendTemplatedEmail(String to, String templateName, Map<String, String> variables);

    List<EmailConfig> getAllConfigs();
    EmailConfig getConfigById(Long id);
    EmailConfig saveConfig(EmailConfigDTO dto);
    EmailConfig enableConfig(Long id);
    void deleteConfig(Long id);

    List<EmailTemplate> getAllTemplates();
    EmailTemplate getTemplateById(Long id);
    EmailTemplate saveTemplate(EmailTemplateDTO dto);
    EmailTemplate resetTemplate(Long id);
    List<String> getTemplateVariables(Long templateId);

    List<EmailLog> getAllLogs();

    Map<String, Object> getRoleNotificationMatrix();
    void updateRoleNotifications(RoleNotificationUpdateDTO dto);

    Map<String, Object> getUserExtraRules(Long userId);
    void updateUserExtraRules(UserExtraRulesUpdateDTO dto);
}
