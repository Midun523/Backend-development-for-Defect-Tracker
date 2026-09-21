package com.defecttracker;

import com.defecttracker.entity.*;
import com.defecttracker.entity.Module;
import com.defecttracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class TestDataFixture {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final ModuleRepository moduleRepository;
    private final SubModuleRepository subModuleRepository;
    private final TestCaseRepository testCaseRepository;
    private final ReleaseRepository releaseRepository;
    private final ReleaseTestCaseRepository releaseTestCaseRepository;
    private final ProjectAllocationRepository projectAllocationRepository;
    private final DefectRepository defectRepository;
    private final DefectCommentRepository defectCommentRepository;
    private final KlocMetricRepository klocMetricRepository;
    private final EmailConfigRepository emailConfigRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final DesignationRepository designationRepository;
    private final PermissionRepository permissionRepository;
    private final SeverityRepository severityRepository;
    private final PriorityRepository priorityRepository;
    private final StatusTypeRepository statusTypeRepository;
    private final DefectTypeRepository defectTypeRepository;
    private final ReleaseTypeRepository releaseTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TestDataFixture(
            ClientRepository clientRepository,
            ProjectRepository projectRepository,
            ModuleRepository moduleRepository,
            SubModuleRepository subModuleRepository,
            TestCaseRepository testCaseRepository,
            ReleaseRepository releaseRepository,
            ReleaseTestCaseRepository releaseTestCaseRepository,
            ProjectAllocationRepository projectAllocationRepository,
            DefectRepository defectRepository,
            DefectCommentRepository defectCommentRepository,
            KlocMetricRepository klocMetricRepository,
            EmailConfigRepository emailConfigRepository,
            EmailTemplateRepository emailTemplateRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            DesignationRepository designationRepository,
            PermissionRepository permissionRepository,
            SeverityRepository severityRepository,
            PriorityRepository priorityRepository,
            StatusTypeRepository statusTypeRepository,
            DefectTypeRepository defectTypeRepository,
            ReleaseTypeRepository releaseTypeRepository,
            PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.projectRepository = projectRepository;
        this.moduleRepository = moduleRepository;
        this.subModuleRepository = subModuleRepository;
        this.testCaseRepository = testCaseRepository;
        this.releaseRepository = releaseRepository;
        this.releaseTestCaseRepository = releaseTestCaseRepository;
        this.projectAllocationRepository = projectAllocationRepository;
        this.defectRepository = defectRepository;
        this.defectCommentRepository = defectCommentRepository;
        this.klocMetricRepository = klocMetricRepository;
        this.emailConfigRepository = emailConfigRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.designationRepository = designationRepository;
        this.permissionRepository = permissionRepository;
        this.severityRepository = severityRepository;
        this.priorityRepository = priorityRepository;
        this.statusTypeRepository = statusTypeRepository;
        this.defectTypeRepository = defectTypeRepository;
        this.releaseTypeRepository = releaseTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record FixtureIds(
            Long clientId,
            Long projectId,
            Long managerEmployeeId,
            Long devEmployeeId,
            Long qaEmployeeId,
            Long moduleId,
            Long subModuleId,
            Long testCaseId,
            Long releaseId,
            Long releaseTestCaseId,
            Long projectAllocationId,
            Long defectId,
            Long defectCommentId,
            Long klocId,
            Long emailConfigId,
            Long emailTemplateId,
            Long roleId,
            Long permissionId,
            Long designationId,
            Long userId
    ) {}

    @Transactional
    public FixtureIds seedFixtureData() {
        // 1. Roles and Designations
        Role devRole = roleRepository.findByRoleName("Developer")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("Developer").type("Engineering").build()));
        Role qaRole = roleRepository.findByRoleName("QA Tester")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("QA Tester").type("Quality").build()));
        Role mgrRole = roleRepository.findByRoleName("Project Manager")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("Project Manager").type("Management").build()));

        Designation devDesig = designationRepository.findByDesignationName("Software Engineer")
                .orElseGet(() -> designationRepository.save(Designation.builder().designationName("Software Engineer").build()));
        Designation qaDesig = designationRepository.findByDesignationName("QA Engineer")
                .orElseGet(() -> designationRepository.save(Designation.builder().designationName("QA Engineer").build()));
        Designation mgrDesig = designationRepository.findByDesignationName("Project Manager")
                .orElseGet(() -> designationRepository.save(Designation.builder().designationName("Project Manager").build()));

        Permission permission = permissionRepository.findAll().stream().findFirst()
                .orElseGet(() -> permissionRepository.save(Permission.builder().action("PROJECT_READ").description("Read projects").build()));

        // 2. Users and Employees
        User mgrUser = userRepository.findByEmail("manager_fixture@defecttracker.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .userId("US_MGR")
                        .firstName("Manager")
                        .lastName("Fixture")
                        .email("manager_fixture@defecttracker.com")
                        .password(passwordEncoder.encode("Password123!"))
                        .userStatus("ACTIVE")
                        .userType("CompanyStaff")
                        .roles(Set.of(mgrRole))
                        .designation(mgrDesig)
                        .build()));

        Employee mgrEmployee = employeeRepository.findByEmail("manager_fixture@defecttracker.com")
                .orElseGet(() -> employeeRepository.save(Employee.builder()
                        .user(mgrUser)
                        .firstName("Manager")
                        .lastName("Fixture")
                        .email("manager_fixture@defecttracker.com")
                        .designation(mgrDesig)
                        .status("active")
                        .build()));

        User devUser = userRepository.findByEmail("dev_fixture@defecttracker.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .userId("US_DEV")
                        .firstName("Developer")
                        .lastName("Fixture")
                        .email("dev_fixture@defecttracker.com")
                        .password(passwordEncoder.encode("Password123!"))
                        .userStatus("ACTIVE")
                        .userType("CompanyStaff")
                        .roles(Set.of(devRole))
                        .designation(devDesig)
                        .build()));

        Employee devEmployee = employeeRepository.findByEmail("dev_fixture@defecttracker.com")
                .orElseGet(() -> employeeRepository.save(Employee.builder()
                        .user(devUser)
                        .firstName("Developer")
                        .lastName("Fixture")
                        .email("dev_fixture@defecttracker.com")
                        .designation(devDesig)
                        .status("active")
                        .build()));

        User qaUser = userRepository.findByEmail("qa_fixture@defecttracker.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .userId("US_QA")
                        .firstName("Tester")
                        .lastName("Fixture")
                        .email("qa_fixture@defecttracker.com")
                        .password(passwordEncoder.encode("Password123!"))
                        .userStatus("ACTIVE")
                        .userType("CompanyStaff")
                        .roles(Set.of(qaRole))
                        .designation(qaDesig)
                        .build()));

        Employee qaEmployee = employeeRepository.findByEmail("qa_fixture@defecttracker.com")
                .orElseGet(() -> employeeRepository.save(Employee.builder()
                        .user(qaUser)
                        .firstName("Tester")
                        .lastName("Fixture")
                        .email("qa_fixture@defecttracker.com")
                        .designation(qaDesig)
                        .status("active")
                        .build()));

        // 3. Client
        Client client = clientRepository.findAll().stream().findFirst()
                .orElseGet(() -> clientRepository.save(Client.builder()
                        .clientName("Fixture Global Client")
                        .email("client_fixture@defecttracker.com")
                        .phoneNumber("+1 555-0100")
                        .country("USA")
                        .state("CA")
                        .build()));

        // 4. Project
        Project project = projectRepository.findAll().stream().findFirst()
                .orElseGet(() -> projectRepository.save(Project.builder()
                        .name("Fixture Core Project")
                        .projectId("PRJ_FIXTURE_1")
                        .status("ACTIVE")
                        .client(client)
                        .manager(mgrEmployee)
                        .build()));

        // 5. Module & SubModule
        Module module = moduleRepository.findAll().stream().findFirst()
                .orElseGet(() -> moduleRepository.save(Module.builder()
                        .name("Fixture Core Module")
                        .description("Module for fixture tests")
                        .project(project)
                        .leader(mgrEmployee)
                        .build()));

        SubModule subModule = subModuleRepository.findAll().stream().findFirst()
                .orElseGet(() -> subModuleRepository.save(SubModule.builder()
                        .name("Fixture Core SubModule")
                        .description("SubModule for fixture tests")
                        .module(module)
                        .build()));

        // 6. Master data
        Severity severity = severityRepository.findAll().stream().findFirst()
                .orElseGet(() -> severityRepository.save(Severity.builder().name("Medium").build()));
        Priority priority = priorityRepository.findAll().stream().findFirst()
                .orElseGet(() -> priorityRepository.save(Priority.builder().name("Medium").build()));
        StatusType statusType = statusTypeRepository.findAll().stream().findFirst()
                .orElseGet(() -> statusTypeRepository.save(StatusType.builder().name("New").build()));
        DefectType defectType = defectTypeRepository.findAll().stream().findFirst()
                .orElseGet(() -> defectTypeRepository.save(DefectType.builder().name("Functional").build()));
        ReleaseType releaseType = releaseTypeRepository.findAll().stream().findFirst()
                .orElseGet(() -> releaseTypeRepository.save(ReleaseType.builder().name("Major").build()));

        // 7. TestCase
        TestCase testCase = testCaseRepository.findAll().stream().findFirst()
                .orElseGet(() -> testCaseRepository.save(TestCase.builder()
                        .testcaseNo("TC_FIXTURE_1")
                        .description("Verify basic workflow")
                        .detailsSteps("Step 1: Open app\nStep 2: Submit")
                        .expectedResult("Success response")
                        .subModule(subModule)
                        .severity(severity)
                        .defectType(defectType)
                        .assignedQa(qaEmployee)
                        .build()));

        // 8. Release & ReleaseTestCase
        Release release = releaseRepository.findAll().stream().findFirst()
                .orElseGet(() -> releaseRepository.save(Release.builder()
                        .releaseNo("REL_FIXTURE_1")
                        .name("Release 1.0 Fixture")
                        .version("v1.0.0")
                        .project(project)
                        .releaseType(releaseType)
                        .status("PLANNED")
                        .build()));

        ReleaseTestCase releaseTestCase = releaseTestCaseRepository.findAll().stream().findFirst()
                .orElseGet(() -> releaseTestCaseRepository.save(ReleaseTestCase.builder()
                        .release(release)
                        .testCase(testCase)
                        .assignedQa(qaEmployee)
                        .executionStatus("PASS")
                        .build()));

        // 9. Project Allocation
        ProjectAllocation projectAllocation = projectAllocationRepository.findAll().stream().findFirst()
                .orElseGet(() -> projectAllocationRepository.save(ProjectAllocation.builder()
                        .project(project)
                        .employee(devEmployee)
                        .role("Developer")
                        .allocationPercentage(100)
                        .status("ACTIVE")
                        .build()));

        // 10. Defect & DefectComment
        Defect defect = defectRepository.findAll().stream().findFirst()
                .orElseGet(() -> defectRepository.save(Defect.builder()
                        .defectId("DEF_FIXTURE_1")
                        .title("Fixture UI Rendering Defect")
                        .description("Sample defect for test fixtures")
                        .project(project)
                        .module(module)
                        .subModule(subModule)
                        .testCase(testCase)
                        .assignedTo(devEmployee)
                        .assignedBy(qaEmployee)
                        .reportedBy(qaUser.getEmail())
                        .severity(severity)
                        .priority(priority)
                        .defectStatus(statusType)
                        .defectType(defectType)
                        .build()));

        DefectComment defectComment = defectCommentRepository.findAll().stream().findFirst()
                .orElseGet(() -> defectCommentRepository.save(DefectComment.builder()
                        .defect(defect)
                        .user(qaUser)
                        .comment("Initial defect comment from QA")
                        .build()));

        // 11. KlocMetric
        KlocMetric klocMetric = klocMetricRepository.findAll().stream().findFirst()
                .orElseGet(() -> klocMetricRepository.save(KlocMetric.builder()
                        .project(project)
                        .defect(defect)
                        .backendRepoUrl("https://github.com/org/backend-fixture")
                        .frontendRepoUrl("https://github.com/org/frontend-fixture")
                        .githubUsername("fixture-user")
                        .calculatedKloc(10.0)
                        .totalLinesOfCode(10000L)
                        .build()));

        // 12. EmailConfig & EmailTemplate
        EmailConfig emailConfig = emailConfigRepository.findAll().stream().findFirst()
                .orElseGet(() -> emailConfigRepository.save(EmailConfig.builder()
                        .name("Fixture SMTP")
                        .smtpHost("smtp.fixture.local")
                        .smtpPort(587)
                        .username("smtp@fixture.local")
                        .password("smtpSecret123!")
                        .fromEmail("notifications@fixture.local")
                        .fromName("Fixture Notifications")
                        .isActive(true)
                        .isDefault(true)
                        .build()));

        EmailTemplate emailTemplate = emailTemplateRepository.findAll().stream().findFirst()
                .orElseGet(() -> emailTemplateRepository.save(EmailTemplate.builder()
                        .templateName("FIXTURE_NOTIFICATION")
                        .subject("Fixture Notification Subject")
                        .bodyContent("Hello {name}, your task is ready.")
                        .eventTrigger("FIXTURE_NOTIFICATION")
                        .variables("name")
                        .isActive(true)
                        .isDefault(true)
                        .build()));

        return new FixtureIds(
                client.getId(),
                project.getId(),
                mgrEmployee.getId(),
                devEmployee.getId(),
                qaEmployee.getId(),
                module.getId(),
                subModule.getId(),
                testCase.getId(),
                release.getId(),
                releaseTestCase.getId(),
                projectAllocation.getId(),
                defect.getId(),
                defectComment.getId(),
                klocMetric.getId(),
                emailConfig.getId(),
                emailTemplate.getId(),
                devRole.getId(),
                permission.getPermissionId(),
                devDesig.getId(),
                devUser.getId()
        );
    }
}
