package com.defecttracker.modules.notification.service;

import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.common.exception.BusinessRuleException;
import com.defecttracker.common.exception.ResourceNotFoundException;
import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.access.entity.Role;
import com.defecttracker.modules.access.repository.EmployeeRepository;
import com.defecttracker.modules.access.repository.RoleRepository;
import com.defecttracker.modules.notification.dto.NotificationDto;
import com.defecttracker.modules.notification.entity.EmailConfiguration;
import com.defecttracker.modules.notification.entity.EmailEmployeeOverride;
import com.defecttracker.modules.notification.entity.EmailLog;
import com.defecttracker.modules.notification.entity.EmailRoleRecipient;
import com.defecttracker.modules.notification.entity.EmailTemplate;
import com.defecttracker.modules.notification.repository.EmailConfigurationRepository;
import com.defecttracker.modules.notification.repository.EmailEmployeeOverrideRepository;
import com.defecttracker.modules.notification.repository.EmailLogRepository;
import com.defecttracker.modules.notification.repository.EmailRoleRecipientRepository;
import com.defecttracker.modules.notification.repository.EmailTemplateRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailConfigurationRepository configRepository;
    private final EmailTemplateRepository templateRepository;
    private final EmailRoleRecipientRepository roleRecipientRepository;
    private final EmailEmployeeOverrideRepository overrideRepository;
    private final EmailLogRepository logRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;

    // --- SMTP Settings ---
    @Transactional(readOnly = true)
    public NotificationDto.EmailConfigDto getEmailConfiguration() {
        EmailConfiguration config = configRepository.findFirstByOrderByIdAsc()
                .orElse(EmailConfiguration.builder()
                        .host("smtp.mailtrap.io").port(2525)
                        .fromEmail("no-reply@defecttracker.com").fromName("DefectTracker Alerts")
                        .authEnabled(true).starttlsEnabled(true).enabled(false)
                        .build());
        return mapConfig(config);
    }

    @Transactional
    public NotificationDto.EmailConfigDto updateEmailConfiguration(NotificationDto.EmailConfigDto dto) {
        EmailConfiguration config = configRepository.findFirstByOrderByIdAsc()
                .orElse(new EmailConfiguration());

        config.setHost(dto.getHost());
        config.setPort(dto.getPort());
        config.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            config.setPassword(dto.getPassword());
        }
        config.setFromEmail(dto.getFromEmail());
        config.setFromName(dto.getFromName());
        config.setAuthEnabled(dto.isAuthEnabled());
        config.setStarttlsEnabled(dto.isStarttlsEnabled());
        config.setEnabled(dto.isEnabled());

        return mapConfig(configRepository.save(config));
    }

    // --- Templates ---
    @Transactional(readOnly = true)
    public List<NotificationDto.EmailTemplateDto> getAllTemplates() {
        return templateRepository.findAll().stream().map(this::mapTemplate).collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto.EmailTemplateDto updateTemplate(Long id, NotificationDto.EmailTemplateDto dto) {
        EmailTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", id));

        template.setSubject(dto.getSubject());
        template.setBody(dto.getBody());
        if (dto.getDescription() != null) template.setDescription(dto.getDescription());

        return mapTemplate(templateRepository.save(template));
    }

    // --- Role Matrix & Employee Overrides ---
    @Transactional(readOnly = true)
    public List<NotificationDto.RoleRecipientMatrixItem> getRoleRecipientMatrix() {
        return roleRecipientRepository.findAll().stream().map(this::mapRoleRecipient).collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto.RoleRecipientMatrixItem setRoleRecipient(NotificationDto.RoleRecipientMatrixItem dto) {
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", dto.getRoleId()));

        EmailRoleRecipient item = roleRecipientRepository
                .findByRoleIdAndNotificationType(role.getId(), dto.getNotificationType())
                .orElseGet(() -> EmailRoleRecipient.builder()
                        .role(role)
                        .notificationType(dto.getNotificationType())
                        .build());

        item.setEnabled(dto.isEnabled());
        return mapRoleRecipient(roleRecipientRepository.save(item));
    }

    @Transactional
    public NotificationDto.EmployeeOverrideItem setEmployeeOverride(NotificationDto.EmployeeOverrideItem dto) {
        Employee emp = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", dto.getEmployeeId()));

        EmailEmployeeOverride item = overrideRepository
                .findByEmployeeIdAndNotificationType(emp.getId(), dto.getNotificationType())
                .orElseGet(() -> EmailEmployeeOverride.builder()
                        .employee(emp)
                        .notificationType(dto.getNotificationType())
                        .build());

        item.setEnabled(dto.isEnabled());
        return mapEmployeeOverride(overrideRepository.save(item));
    }

    // --- Sent Email Logs ---
    @Transactional(readOnly = true)
    public PageResponse<NotificationDto.EmailLogResponse> getEmailLogs(Pageable pageable) {
        Page<EmailLog> page = logRepository.findAllByOrderBySentAtDesc(pageable);
        return PageResponse.of(page.map(this::mapLog));
    }

    // --- Sending & Template Substitution Engine ---
    @Async
    @Transactional
    public void sendTemplatedNotification(String notificationType, Map<String, String> variables, Set<String> directRecipients) {
        EmailConfiguration config = configRepository.findFirstByOrderByIdAsc().orElse(null);
        if (config == null || !config.isEnabled()) {
            log.debug("Email delivery disabled or not configured. Skipping notification type: {}", notificationType);
            return;
        }

        EmailTemplate template = templateRepository.findByNotificationType(notificationType).orElse(null);
        if (template == null) {
            log.warn("Email template not found for notification type: {}", notificationType);
            return;
        }

        // Calculate resolved recipients: Role matrix + Direct + Employee Overrides
        Set<String> recipientEmails = new HashSet<>();
        if (directRecipients != null) {
            recipientEmails.addAll(directRecipients);
        }

        // Add role-based recipients
        List<EmailRoleRecipient> roleSettings = roleRecipientRepository.findByNotificationTypeAndEnabledTrue(notificationType);
        for (EmailRoleRecipient r : roleSettings) {
            List<Employee> emps = employeeRepository.findByActiveTrue().stream()
                    .filter(e -> e.getRole().getId().equals(r.getRole().getId()))
                    .collect(Collectors.toList());
            for (Employee e : emps) {
                recipientEmails.add(e.getEmail());
            }
        }

        // Apply employee overrides (force opt-in or force mute)
        List<EmailEmployeeOverride> overrides = overrideRepository.findByNotificationType(notificationType);
        for (EmailEmployeeOverride ov : overrides) {
            if (ov.isEnabled()) {
                recipientEmails.add(ov.getEmployee().getEmail());
            } else {
                recipientEmails.remove(ov.getEmployee().getEmail());
            }
        }

        // Interpolate template variables
        String subject = interpolate(template.getSubject(), variables);
        String body = interpolate(template.getBody(), variables);

        JavaMailSenderImpl mailSender = createMailSender(config);

        for (String recipient : recipientEmails) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(config.getFromEmail(), config.getFromName());
                helper.setTo(recipient);
                helper.setSubject(subject);
                helper.setText(body, false);

                mailSender.send(message);

                logRepository.save(EmailLog.builder()
                        .recipient(recipient)
                        .subject(subject)
                        .body(body)
                        .notificationType(notificationType)
                        .status("SENT")
                        .sentAt(LocalDateTime.now())
                        .build());

                log.info("Sent email notification [{}] to {}", notificationType, recipient);
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", recipient, e.getMessage());
                logRepository.save(EmailLog.builder()
                        .recipient(recipient)
                        .subject(subject)
                        .body(body)
                        .notificationType(notificationType)
                        .status("FAILED")
                        .errorMessage(e.getMessage())
                        .sentAt(LocalDateTime.now())
                        .build());
            }
        }
    }

    private String interpolate(String text, Map<String, String> variables) {
        if (text == null || variables == null) return text;
        String result = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

    private JavaMailSenderImpl createMailSender(EmailConfiguration config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        if (config.getUsername() != null) mailSender.setUsername(config.getUsername());
        if (config.getPassword() != null) mailSender.setPassword(config.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(config.isAuthEnabled()));
        props.put("mail.smtp.starttls.enable", String.valueOf(config.isStarttlsEnabled()));
        props.put("mail.debug", "false");

        return mailSender;
    }

    private NotificationDto.EmailConfigDto mapConfig(EmailConfiguration c) {
        return NotificationDto.EmailConfigDto.builder()
                .id(c.getId()).host(c.getHost()).port(c.getPort())
                .username(c.getUsername()).fromEmail(c.getFromEmail()).fromName(c.getFromName())
                .authEnabled(c.isAuthEnabled()).starttlsEnabled(c.isStarttlsEnabled()).enabled(c.isEnabled())
                .build();
    }

    private NotificationDto.EmailTemplateDto mapTemplate(EmailTemplate t) {
        return NotificationDto.EmailTemplateDto.builder()
                .id(t.getId()).notificationType(t.getNotificationType())
                .subject(t.getSubject()).body(t.getBody()).description(t.getDescription())
                .build();
    }

    private NotificationDto.RoleRecipientMatrixItem mapRoleRecipient(EmailRoleRecipient r) {
        return NotificationDto.RoleRecipientMatrixItem.builder()
                .id(r.getId()).roleId(r.getRole().getId()).roleName(r.getRole().getName())
                .notificationType(r.getNotificationType()).enabled(r.isEnabled())
                .build();
    }

    private NotificationDto.EmployeeOverrideItem mapEmployeeOverride(EmailEmployeeOverride ov) {
        return NotificationDto.EmployeeOverrideItem.builder()
                .id(ov.getId()).employeeId(ov.getEmployee().getId()).employeeName(ov.getEmployee().getFullName())
                .notificationType(ov.getNotificationType()).enabled(ov.isEnabled())
                .build();
    }

    private NotificationDto.EmailLogResponse mapLog(EmailLog l) {
        return NotificationDto.EmailLogResponse.builder()
                .id(l.getId()).recipient(l.getRecipient()).subject(l.getSubject())
                .body(l.getBody()).notificationType(l.getNotificationType())
                .status(l.getStatus()).errorMessage(l.getErrorMessage()).sentAt(l.getSentAt())
                .build();
    }
}
