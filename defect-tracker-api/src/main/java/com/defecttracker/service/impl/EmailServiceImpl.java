package com.defecttracker.service.impl;

import com.defecttracker.dto.request.EmailConfigDTO;
import com.defecttracker.dto.request.EmailTemplateDTO;
import com.defecttracker.dto.request.RoleNotificationUpdateDTO;
import com.defecttracker.dto.request.UserExtraRulesUpdateDTO;
import com.defecttracker.entity.*;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.*;
import com.defecttracker.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailConfigRepository emailConfigRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailLogRepository emailLogRepository;
    private final RoleNotificationSettingRepository roleNotificationSettingRepository;
    private final UserExtraRuleRepository userExtraRuleRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    @Async
    public boolean sendEmail(String to, String subject, String body) {
        if (to == null || to.trim().isEmpty()) {
            return false;
        }

        EmailConfig config = emailConfigRepository.findFirstByIsDefaultTrue()
                .or(() -> emailConfigRepository.findFirstByIsActiveTrue())
                .orElse(null);

        if (config == null || !config.isActive()) {
            log.warn("No active EmailConfig configured. Mocking email delivery to {}", to);
            logEmail(to, subject, "SENT", null);
            return true;
        }

        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(config.getSmtpHost());
            mailSender.setPort(config.getSmtpPort());
            if (config.getUsername() != null && !config.getUsername().trim().isEmpty()) {
                mailSender.setUsername(config.getUsername());
                mailSender.setPassword(config.getPassword());
            }

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", config.getUsername() != null && !config.getUsername().isEmpty() ? "true" : "false");
            props.put("mail.smtp.starttls.enable", config.isUseTls() ? "true" : "false");
            props.put("mail.smtp.ssl.enable", config.isUseSsl() ? "true" : "false");
            props.put("mail.debug", "false");

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(body, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(config.getFromEmail(), config.getFromName() != null ? config.getFromName() : "Defect Tracker");

            mailSender.send(mimeMessage);
            log.info("Email successfully dispatched to {}", to);
            logEmail(to, subject, "SENT", null);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
            logEmail(to, subject, "FAILED", ex.getMessage());
            return false;
        }
    }

    private void logEmail(String recipient, String subject, String status, String error) {
        try {
            EmailLog emailLog = EmailLog.builder()
                    .recipientEmail(recipient)
                    .subject(subject)
                    .status(status)
                    .errorMessage(error)
                    .build();
            emailLogRepository.save(emailLog);
        } catch (Exception e) {
            log.error("Could not write email log", e);
        }
    }

    @Override
    public boolean sendTemplatedEmail(String to, String templateName, Map<String, String> variables) {
        EmailTemplate template = emailTemplateRepository.findByTemplateName(templateName)
                .or(() -> emailTemplateRepository.findByEventTrigger(templateName))
                .orElse(null);

        if (template == null) {
            log.warn("Template not found for: {}", templateName);
            return false;
        }

        String subject = template.getSubject();
        String body = template.getBodyContent();

        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                subject = subject.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
                body = body.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
            }
        }

        return sendEmail(to, subject, body);
    }

    @Override
    public List<EmailConfig> getAllConfigs() {
        return emailConfigRepository.findAll();
    }

    @Override
    public EmailConfig getConfigById(Long id) {
        return emailConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailConfig", "id", id));
    }

    @Override
    @Transactional
    public EmailConfig saveConfig(EmailConfigDTO dto) {
        EmailConfig config;
        if (dto.getId() != null && dto.getId() > 0) {
            config = getConfigById(dto.getId());
        } else {
            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                throw new BadRequestException("Password is required for new SMTP configuration");
            }
            config = new EmailConfig();
        }

        config.setName(dto.getName());
        config.setSmtpHost(dto.getSmtpHost());
        config.setSmtpPort(dto.getSmtpPort());
        config.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            config.setPassword(dto.getPassword());
        }
        config.setFromEmail(dto.getFromEmail());
        config.setFromName(dto.getFromName());
        config.setActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        config.setDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        config.setUseTls(dto.getUseTls() != null ? dto.getUseTls() : true);
        config.setUseSsl(dto.getUseSsl() != null ? dto.getUseSsl() : false);

        if (config.isDefault()) {
            List<EmailConfig> all = emailConfigRepository.findAll();
            for (EmailConfig ec : all) {
                if (!ec.getId().equals(config.getId())) {
                    ec.setDefault(false);
                    emailConfigRepository.save(ec);
                }
            }
        }

        return emailConfigRepository.save(config);
    }

    @Override
    @Transactional
    public EmailConfig enableConfig(Long id) {
        EmailConfig config = getConfigById(id);
        config.setActive(true);
        config.setDefault(true);

        List<EmailConfig> all = emailConfigRepository.findAll();
        for (EmailConfig ec : all) {
            if (!ec.getId().equals(id)) {
                ec.setDefault(false);
                emailConfigRepository.save(ec);
            }
        }
        return emailConfigRepository.save(config);
    }

    @Override
    public void deleteConfig(Long id) {
        emailConfigRepository.deleteById(id);
    }

    @Override
    public List<EmailTemplate> getAllTemplates() {
        return emailTemplateRepository.findAll();
    }

    @Override
    public EmailTemplate getTemplateById(Long id) {
        return emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", id));
    }

    @Override
    public EmailTemplate saveTemplate(EmailTemplateDTO dto) {
        EmailTemplate template;
        if (dto.getId() != null && dto.getId() > 0) {
            template = getTemplateById(dto.getId());
        } else {
            template = new EmailTemplate();
        }

        template.setTemplateName(dto.getTemplateName());
        template.setSubject(dto.getSubject());
        template.setBodyContent(dto.getBodyContent());
        template.setEventTrigger(dto.getEventTrigger());
        template.setVariables(dto.getVariables());
        template.setActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        template.setDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);

        return emailTemplateRepository.save(template);
    }

    @Override
    public EmailTemplate resetTemplate(Long id) {
        EmailTemplate template = getTemplateById(id);
        template.setBodyContent("Hello {name},\n\nThis is an automated notification regarding defect {defectId}.\n\nRegards,\nTeam");
        return emailTemplateRepository.save(template);
    }

    @Override
    public List<String> getTemplateVariables(Long templateId) {
        EmailTemplate template = getTemplateById(templateId);
        if (template.getVariables() != null) {
            return Arrays.asList(template.getVariables().split(","));
        }
        return List.of("name", "email", "defectId", "projectName", "status", "assignedTo");
    }

    @Override
    public List<EmailLog> getAllLogs() {
        return emailLogRepository.findAllByOrderBySentAtDesc();
    }

    @Override
    public Map<String, Object> getRoleNotificationMatrix() {
        List<Role> roles = roleRepository.findAll();
        List<RoleNotificationSetting> settings = roleNotificationSettingRepository.findAll();
        Map<String, Object> matrix = new HashMap<>();
        matrix.put("roles", roles);
        matrix.put("settings", settings);
        return matrix;
    }

    @Override
    @Transactional
    public void updateRoleNotifications(RoleNotificationUpdateDTO dto) {
        if (dto.getRoleId() == null) return;
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", dto.getRoleId()));

        if (dto.getEmailNotifications() != null) {
            for (Map.Entry<String, Boolean> entry : dto.getEmailNotifications().entrySet()) {
                RoleNotificationSetting setting = roleNotificationSettingRepository
                        .findByRoleIdAndPointKey(role.getId(), entry.getKey())
                        .orElse(RoleNotificationSetting.builder().role(role).pointKey(entry.getKey()).build());
                setting.setEmailEnabled(entry.getValue());
                roleNotificationSettingRepository.save(setting);
            }
        }
    }

    @Override
    public Map<String, Object> getUserExtraRules(Long userId) {
        List<UserExtraRule> rules = userExtraRuleRepository.findByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("rules", rules);
        return result;
    }

    @Override
    @Transactional
    public void updateUserExtraRules(UserExtraRulesUpdateDTO dto) {
        if (dto.getUserId() == null || dto.getRules() == null) return;
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getUserId()));

        for (Map.Entry<String, Boolean> entry : dto.getRules().entrySet()) {
            UserExtraRule rule = userExtraRuleRepository
                    .findByUserIdAndRuleKey(user.getId(), entry.getKey())
                    .orElse(UserExtraRule.builder().user(user).ruleKey(entry.getKey()).build());
            rule.setEnabled(entry.getValue());
            userExtraRuleRepository.save(rule);
        }
    }
}
