// Central Mock Database for Defect Tracker Pro
// Contains comprehensive mock datasets and stateful helpers for offline/standalone mode.

export interface MockUser {
  id: number;
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  userGender?: string;
  userStatus: string;
  designationId?: number;
  designationName?: string;
  roleId?: number;
  roleName?: string;
  roles?: string[];
  userType?: string;
  availabilityPercent?: number;
  skills?: string[];
  currentProjects?: string[];
  joinedDate?: string;
  experience?: number;
  department?: string;
  manager?: string;
  address?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface MockProject {
  id: string | number;
  projectId?: string | number;
  name?: string;
  projectName?: string;
  prefix?: string;
  projectType?: string;
  status?: string;
  projectStatus?: string;
  startDate?: string;
  endDate?: string;
  manager?: string | number;
  userId?: number;
  userFirstName?: string;
  userLastName?: string;
  clientName?: string;
  country?: string;
  clientCountry?: string;
  state?: string;
  clientState?: string;
  email?: string;
  clientEmail?: string;
  phoneNo?: string;
  clientPhone?: string;
  address?: string;
  description?: string;
  progress?: number;
  kloc?: number;
  teamMembers?: string[];
}

export interface MockRelease {
  id: number | string;
  releaseId?: number | string;
  name?: string;
  releaseName?: string;
  version?: string;
  releaseVersion?: string;
  description?: string;
  projectId: number;
  projectName?: string;
  status: string;
  releaseStatus?: string;
  releaseTypeId?: number;
  releaseTypeName?: string;
  startDate?: string;
  releaseDate?: string;
  endDate?: string;
  kloc?: number;
  totalTestCases?: number;
  passedTestCases?: number;
  failedTestCases?: number;
  blockedTestCases?: number;
  unexecutedTestCases?: number;
  features?: string[];
  bugFixes?: string[];
  testCases?: number[];
  createdAt?: string;
}

export interface MockModule {
  id: number;
  moduleId?: number;
  name: string;
  moduleName?: string;
  projectId: number;
  description?: string;
  leaderId?: number | null;
  leaderName?: string | null;
  allocatedLeader?: {
    id: number;
    employeeId: number;
    employeeName: string;
    allocatedDate: string;
  } | null;
  assignedDevs?: string[];
  submodules?: MockSubmodule[];
}

export interface MockSubmodule {
  id: number;
  subModuleId?: number;
  name: string;
  subModuleName?: string;
  moduleId: number;
  description?: string;
  assignedDevs?: Array<{
    id: number;
    employeeId: number;
    employeeName: string;
    assignedDate?: string;
  }>;
}

export interface MockTestCase {
  id: number;
  testcaseNo: string;
  no?: string;
  description: string;
  detailsSteps: string;
  steps?: string;
  expectedResult: string;
  subModuleId: number;
  subModuleName: string;
  moduleId?: number;
  moduleName?: string;
  projectId?: number;
  severityId: number;
  severityName: string;
  severity?: string;
  defectTypeId: number;
  defectTypeName: string;
  type?: string;
  executionStatus?: 'PASS' | 'FAIL' | 'BLOCKED' | 'NOT_RUN' | 'HOLD';
  assignedQaId?: number | null;
  assignedQaName?: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy?: string | null;
  updatedBy?: string | null;
}

export interface MockDefect {
  id: number;
  defectId: string;
  title: string;
  description: string;
  priority: 'low' | 'medium' | 'high' | 'critical';
  priorityId?: number;
  priorityName?: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  severityId?: number;
  severityName?: string;
  status: 'new' | 'open' | 'in-progress' | 'resolved' | 'closed' | 'rejected' | string;
  statusId?: number;
  statusName?: string;
  defectStatusId?: number;
  defectStatusName?: string;
  projectId: number;
  projectName?: string;
  releaseId?: number | string;
  releaseName?: string;
  moduleId?: number;
  moduleName?: string;
  subModuleId?: number;
  subModuleName?: string;
  testCaseId?: number | null;
  steps?: string;
  stepsToReproduce?: string[];
  reOpenCount: number;
  reopenCount?: number;
  attachment?: string | null;
  attachments?: string[];
  reportedBy?: string;
  assignedTo?: string;
  assignedToId?: number;
  assignedToName?: string;
  assignedById?: number;
  assignedByName?: string;
  createdAt: string;
  updatedAt: string;
  defectHistory?: Array<{
    id?: number;
    status: string;
    changedAt: string;
    comment?: string;
    changedBy?: string;
  }>;
  comments?: Array<{
    id: number;
    defectId: number;
    userId: number;
    userName: string;
    userAvatar?: string;
    comment: string;
    createdAt: string;
  }>;
}

// Initial Mock Designations (Empty - no predefined designations)
export const INITIAL_DESIGNATIONS: any[] = [];

// Initial Mock Roles (Empty - no predefined roles)
export const INITIAL_ROLES: any[] = [];

// Initial Permissions
export const INITIAL_PERMISSION_MODULES = [
  {
    module: "Designation",
    permissions: [
      { permissionId: 1, action: "CREATE", description: "Create Designation" },
      { permissionId: 2, action: "UPDATE", description: "Update Designation" },
      { permissionId: 3, action: "READ", description: "View Designation" },
      { permissionId: 4, action: "DELETE", description: "Delete Designation" },
    ]
  },
  {
    module: "Role",
    permissions: [
      { permissionId: 5, action: "CREATE", description: "Create Role" },
      { permissionId: 6, action: "UPDATE", description: "Update Role" },
      { permissionId: 7, action: "READ", description: "View Role" },
      { permissionId: 8, action: "DELETE", description: "Delete Role" },
    ]
  },
  {
    module: "Privileges",
    permissions: [
      { permissionId: 9, action: "READ", description: "View Permissions" },
      { permissionId: 10, action: "ASSIGN", description: "Assign Permissions to Role" },
      { permissionId: 11, action: "ROLE_PERMISSION_READ", description: "View Role Permissions" },
    ]
  },
  {
    module: "Defect Type",
    permissions: [
      { permissionId: 12, action: "CREATE", description: "Create Defect Type" },
      { permissionId: 13, action: "UPDATE", description: "Update Defect Type" },
      { permissionId: 14, action: "READ", description: "View Defect Type" },
      { permissionId: 15, action: "DELETE", description: "Delete Defect Type" },
    ]
  },
  {
    module: "Release Type",
    permissions: [
      { permissionId: 16, action: "CREATE", description: "Create Release Type" },
      { permissionId: 17, action: "UPDATE", description: "Update Release Type" },
      { permissionId: 18, action: "READ", description: "View Release Type" },
      { permissionId: 19, action: "DELETE", description: "Delete Release Type" },
    ]
  },
  {
    module: "Severity",
    permissions: [
      { permissionId: 20, action: "CREATE", description: "Create Severity" },
      { permissionId: 21, action: "UPDATE", description: "Update Severity" },
      { permissionId: 22, action: "READ", description: "View Severity" },
      { permissionId: 23, action: "DELETE", description: "Delete Severity" },
    ]
  },
  {
    module: "Priority",
    permissions: [
      { permissionId: 24, action: "CREATE", description: "Create Priority" },
      { permissionId: 25, action: "UPDATE", description: "Update Priority" },
      { permissionId: 26, action: "READ", description: "View Priority" },
      { permissionId: 27, action: "DELETE", description: "Delete Priority" },
    ]
  },
  {
    module: "Status Type",
    permissions: [
      { permissionId: 28, action: "CREATE", description: "Create Status Type" },
      { permissionId: 29, action: "UPDATE", description: "Update Status Type" },
      { permissionId: 30, action: "READ", description: "View Status Type" },
      { permissionId: 31, action: "DELETE", description: "Delete Status Type" },
    ]
  },
  {
    module: "Status Workflow",
    permissions: [
      { permissionId: 32, action: "CREATE", description: "Create Workflow" },
      { permissionId: 33, action: "READ", description: "View Workflow" },
    ]
  },
  {
    module: "Employee",
    permissions: [
      { permissionId: 34, action: "CREATE", description: "Create Employee" },
      { permissionId: 35, action: "UPDATE", description: "Update Employee" },
      { permissionId: 36, action: "READ", description: "View Employee" },
      { permissionId: 37, action: "DELETE", description: "Delete Employee" },
      { permissionId: 38, action: "BENCH_READ", description: "View Bench" },
    ]
  },
  {
    module: "Project",
    permissions: [
      { permissionId: 39, action: "CREATE", description: "Create Project" },
      { permissionId: 40, action: "UPDATE", description: "Update Project" },
      { permissionId: 41, action: "READ", description: "View Project" },
      { permissionId: 42, action: "DELETE", description: "Delete Project" },
    ]
  },
  {
    module: "Module",
    permissions: [
      { permissionId: 43, action: "CREATE", description: "Create Module" },
      { permissionId: 44, action: "UPDATE", description: "Update Module" },
      { permissionId: 45, action: "READ", description: "View Module" },
      { permissionId: 46, action: "DELETE", description: "Delete Module" },
    ]
  },
  {
    module: "Test Case",
    permissions: [
      { permissionId: 47, action: "CREATE", description: "Create Test Case" },
      { permissionId: 48, action: "UPDATE", description: "Update Test Case" },
      { permissionId: 49, action: "READ", description: "View Test Case" },
      { permissionId: 50, action: "DELETE", description: "Delete Test Case" },
    ]
  },
  {
    module: "Release",
    permissions: [
      { permissionId: 51, action: "CREATE", description: "Create Release" },
      { permissionId: 52, action: "UPDATE", description: "Update Release" },
      { permissionId: 53, action: "READ", description: "View Release" },
      { permissionId: 54, action: "DELETE", description: "Delete Release" },
    ]
  },
  {
    module: "Defect",
    permissions: [
      { permissionId: 55, action: "CREATE", description: "Create Defect" },
      { permissionId: 56, action: "UPDATE", description: "Update Defect" },
      { permissionId: 57, action: "READ", description: "View Defect" },
      { permissionId: 58, action: "DELETE", description: "Delete Defect" },
      { permissionId: 59, action: "ASSIGN", description: "Assign Developer" },
      { permissionId: 60, action: "STATUS_CHANGE", description: "Change Defect Status" },
      { permissionId: 61, action: "COMMENT_CREATE", description: "Add Comment" },
      { permissionId: 62, action: "COMMENT_READ", description: "View Comments" },
    ]
  },
  {
    module: "Email Config",
    permissions: [
      { permissionId: 63, action: "READ", description: "View Email Config" },
      { permissionId: 64, action: "CREATE", description: "Create Email Config" },
      { permissionId: 65, action: "UPDATE", description: "Update Email Config" },
      { permissionId: 66, action: "DELETE", description: "Delete Email Config" },
      { permissionId: 67, action: "ALL_PERMISSIONS", description: "Full Admin Access" },
    ]
  }
];

export const INITIAL_PERMISSIONS = INITIAL_PERMISSION_MODULES.flatMap(m => m.permissions);

// Initial Severities (Empty - no predefined severities)
export const INITIAL_SEVERITIES: any[] = [];

// Initial Priorities (Empty - no predefined priorities)
export const INITIAL_PRIORITIES: any[] = [];

// Initial Defect Types (Empty - no predefined defect types)
export const INITIAL_DEFECT_TYPES: any[] = [];

// Initial Release Types (Empty - no predefined release types)
export const INITIAL_RELEASE_TYPES: any[] = [];

// Initial Status Types (Empty - no predefined status types)
export const INITIAL_STATUS_TYPES: any[] = [];

// // Initial Users & Employees (Only default Super Admin matching backend)
export const INITIAL_USERS: MockUser[] = [
  {
    id: 1,
    userId: 'US0001',
    firstName: 'Super',
    lastName: 'Admin',
    email: 'admin@defecttracker.com',
    phone: '+1 555-0199',
    userGender: 'Male',
    userStatus: 'ACTIVE',
    designationId: undefined,
    designationName: undefined,
    roleId: 1,
    roleName: 'Super Admin',
    roles: ['Super Admin'],
    userType: 'CompanyStaff',
    availabilityPercent: 100,
    skills: [],
    currentProjects: [],
    joinedDate: '2023-01-01',
    experience: 5,
    department: 'Engineering',
    address: 'Headquarters',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

// Initial Projects (Empty - no predefined projects)
export const INITIAL_PROJECTS: MockProject[] = [];

// Initial Releases (Empty - no predefined releases)
export const INITIAL_RELEASES: MockRelease[] = [];

// Initial Modules & Submodules (Empty - no predefined modules)
export const INITIAL_MODULES: MockModule[] = [];

// Initial Test Cases (Empty - no predefined test cases)
export const INITIAL_TEST_CASES: MockTestCase[] = [];

// Initial Defects (Empty - no predefined defects)
export const INITIAL_DEFECTS: MockDefect[] = [];

// Initial Bench Allocations (Empty - no predefined bench resources)
export const INITIAL_BENCH_RESOURCES: any[] = [];

// Initial Email Configurations (Empty - no predefined email configs)
export const INITIAL_EMAIL_CONFIGS: any[] = [];

// Initial Email Point Setup & Templates (Empty - no predefined templates)
export const INITIAL_EMAIL_POINT_SETUPS: any[] = [];

export const INITIAL_EMAIL_TEMPLATES: any[] = [];

// State Store Class for managing Mock Data statefully in memory / localStorage
class MockDbStore {
  private users: MockUser[] = [...INITIAL_USERS];
  private projects: MockProject[] = [...INITIAL_PROJECTS];
  private releases: MockRelease[] = [...INITIAL_RELEASES];
  private modules: MockModule[] = [...INITIAL_MODULES];
  private testCases: MockTestCase[] = [...INITIAL_TEST_CASES];
  private defects: MockDefect[] = [...INITIAL_DEFECTS];
  private designations = [...INITIAL_DESIGNATIONS];
  private roles = [...INITIAL_ROLES];
  private permissions = [...INITIAL_PERMISSIONS];
  private severities = [...INITIAL_SEVERITIES];
  private priorities = [...INITIAL_PRIORITIES];
  private defectTypes = [...INITIAL_DEFECT_TYPES];
  private releaseTypes = [...INITIAL_RELEASE_TYPES];
  private statusTypes = [...INITIAL_STATUS_TYPES];
  private benchResources = [...INITIAL_BENCH_RESOURCES];
  private emailConfigs = [...INITIAL_EMAIL_CONFIGS];
  private emailPointSetups = [...INITIAL_EMAIL_POINT_SETUPS];
  private emailTemplates = [...INITIAL_EMAIL_TEMPLATES];

  constructor() {
    this.loadFromStorage();
  }

  private loadFromStorage() {
    try {
      // If legacy mock cache exists from before sample data removal, wipe it
      const isCleaned = localStorage.getItem('mock_db_clean_v11_live');
      if (!isCleaned) {
        localStorage.removeItem('mock_projects');
        localStorage.removeItem('mock_defects');
        localStorage.removeItem('mock_testcases');
        localStorage.removeItem('mock_modules');
        localStorage.removeItem('mock_releases');
        localStorage.removeItem('mock_users');
        localStorage.removeItem('mock_designations');
        localStorage.removeItem('mock_roles');
        localStorage.removeItem('mock_severities');
        localStorage.removeItem('mock_priorities');
        localStorage.removeItem('mock_defect_types');
        localStorage.removeItem('mock_release_types');
        localStorage.removeItem('mock_status_types');
        localStorage.removeItem('mock_email_configs');
        localStorage.removeItem('mock_email_templates');
        localStorage.removeItem('mock_email_point_setups');
        localStorage.removeItem('mock_email_points');
        localStorage.removeItem('statusWorkflowNodes');
        localStorage.removeItem('statusWorkflowEdges');
        localStorage.removeItem('statusWorkflowLayout');
        localStorage.removeItem('assignments');
        localStorage.removeItem('mockReleases');
        localStorage.removeItem('mockTestCases');
        localStorage.removeItem('mockQA');
        localStorage.removeItem('qaAllocatedTestCases');
        localStorage.removeItem('qaAllocations');
        localStorage.removeItem('testCaseDefectMapping');
        localStorage.removeItem('defectAssignments');
        localStorage.removeItem('email_notification_rules');
        for (let i = 1; i <= 10; i++) {
          localStorage.removeItem(`mock_db_clean_v${i}`);
        }
        localStorage.setItem('mock_db_clean_v11_live', 'true');
      }

      const savedProjects = localStorage.getItem('mock_projects');
      if (savedProjects) this.projects = JSON.parse(savedProjects);

      const savedDefects = localStorage.getItem('mock_defects');
      if (savedDefects) this.defects = JSON.parse(savedDefects);

      const savedTestCases = localStorage.getItem('mock_testcases');
      if (savedTestCases) this.testCases = JSON.parse(savedTestCases);

      const savedModules = localStorage.getItem('mock_modules');
      if (savedModules) this.modules = JSON.parse(savedModules);

      const savedReleases = localStorage.getItem('mock_releases');
      if (savedReleases) this.releases = JSON.parse(savedReleases);

      const savedUsers = localStorage.getItem('mock_users');
      if (savedUsers) this.users = JSON.parse(savedUsers);

      const savedDesignations = localStorage.getItem('mock_designations');
      if (savedDesignations) this.designations = JSON.parse(savedDesignations);

      const savedRoles = localStorage.getItem('mock_roles');
      if (savedRoles) this.roles = JSON.parse(savedRoles);

      const savedSeverities = localStorage.getItem('mock_severities');
      if (savedSeverities) this.severities = JSON.parse(savedSeverities);

      const savedPriorities = localStorage.getItem('mock_priorities');
      if (savedPriorities) this.priorities = JSON.parse(savedPriorities);

      const savedDefectTypes = localStorage.getItem('mock_defect_types');
      if (savedDefectTypes) this.defectTypes = JSON.parse(savedDefectTypes);

      const savedReleaseTypes = localStorage.getItem('mock_release_types');
      if (savedReleaseTypes) this.releaseTypes = JSON.parse(savedReleaseTypes);

      const savedStatusTypes = localStorage.getItem('mock_status_types');
      if (savedStatusTypes) this.statusTypes = JSON.parse(savedStatusTypes);
    } catch {
      // Fallback to in-memory initial data
    }
  }

  private saveToStorage() {
    try {
      localStorage.setItem('mock_projects', JSON.stringify(this.projects));
      localStorage.setItem('mock_defects', JSON.stringify(this.defects));
      localStorage.setItem('mock_testcases', JSON.stringify(this.testCases));
      localStorage.setItem('mock_modules', JSON.stringify(this.modules));
      localStorage.setItem('mock_releases', JSON.stringify(this.releases));
      localStorage.setItem('mock_users', JSON.stringify(this.users));
      localStorage.setItem('mock_designations', JSON.stringify(this.designations));
      localStorage.setItem('mock_roles', JSON.stringify(this.roles));
      localStorage.setItem('mock_severities', JSON.stringify(this.severities));
      localStorage.setItem('mock_priorities', JSON.stringify(this.priorities));
      localStorage.setItem('mock_defect_types', JSON.stringify(this.defectTypes));
      localStorage.setItem('mock_release_types', JSON.stringify(this.releaseTypes));
      localStorage.setItem('mock_status_types', JSON.stringify(this.statusTypes));
    } catch {
      // Ignore storage errors in restricted contexts
    }
  }

  // --- Users & Employees ---
  getUsers() { return [...this.users]; }
  getUserById(id: number) { return this.users.find(u => u.id === Number(id)); }
  createUser(userData: Partial<MockUser>) {
    const newUser: MockUser = {
      id: Date.now(),
      userId: userData.userId || `US${Math.floor(1000 + Math.random() * 9000)}`,
      firstName: userData.firstName || '',
      lastName: userData.lastName || '',
      email: userData.email || '',
      phone: userData.phone || '',
      userGender: userData.userGender || 'Male',
      userStatus: userData.userStatus || 'ACTIVE',
      designationId: userData.designationId || 3,
      designationName: userData.designationName || 'Software Engineer',
      roleId: userData.roleId || 4,
      roleName: userData.roleName || 'Developer',
      roles: userData.roles || ['Developer'],
      userType: userData.userType || 'Employee',
      availabilityPercent: userData.availabilityPercent ?? 100,
      skills: userData.skills || [],
      currentProjects: userData.currentProjects || [],
      joinedDate: userData.joinedDate || new Date().toISOString().split('T')[0],
      experience: userData.experience || 1,
      department: userData.department || 'Engineering',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      ...userData,
    };
    this.users.unshift(newUser);
    this.saveToStorage();
    return newUser;
  }
  updateUser(id: number, updates: Partial<MockUser>) {
    const idx = this.users.findIndex(u => u.id === Number(id));
    if (idx !== -1) {
      this.users[idx] = { ...this.users[idx], ...updates, updatedAt: new Date().toISOString() };
      this.saveToStorage();
      return this.users[idx];
    }
    return null;
  }
  deleteUser(id: number) {
    this.users = this.users.filter(u => u.id !== Number(id));
    this.saveToStorage();
    return true;
  }

  // --- Projects ---
  getProjects() { return [...this.projects]; }
  getProjectById(id: number | string) {
    return this.projects.find(p => String(p.id) === String(id) || String(p.projectId) === String(id));
  }
  createProject(projectData: Partial<MockProject>) {
    const newId = Date.now();
    const newProject: MockProject = {
      id: newId,
      projectId: newId,
      name: projectData.name || projectData.projectName || 'New Project',
      projectName: projectData.name || projectData.projectName || 'New Project',
      prefix: projectData.prefix || 'PRJ',
      projectType: projectData.projectType || 'Web Application',
      status: projectData.status || 'ACTIVE',
      projectStatus: projectData.projectStatus || 'ACTIVE',
      startDate: projectData.startDate || new Date().toISOString().split('T')[0],
      endDate: projectData.endDate || new Date(Date.now() + 180 * 24 * 3600 * 1000).toISOString().split('T')[0],
      manager: projectData.manager || 'Super Admin',
      userId: projectData.userId || 1,
      userFirstName: projectData.userFirstName || 'Super',
      userLastName: projectData.userLastName || 'Admin',
      clientName: projectData.clientName || 'Client Inc.',
      country: projectData.country || projectData.clientCountry || 'United States',
      clientCountry: projectData.clientCountry || projectData.country || 'United States',
      state: projectData.state || projectData.clientState || 'California',
      clientState: projectData.clientState || projectData.state || 'California',
      email: projectData.email || projectData.clientEmail || 'client@example.com',
      clientEmail: projectData.clientEmail || projectData.email || 'client@example.com',
      phoneNo: projectData.phoneNo || projectData.clientPhone || '+1 555 0199',
      clientPhone: projectData.clientPhone || projectData.phoneNo || '+1 555 0199',
      address: projectData.address || 'Headquarters',
      description: projectData.description || '',
      progress: projectData.progress ?? 0,
      kloc: projectData.kloc ?? 0,
      teamMembers: projectData.teamMembers || [],
      ...projectData,
    };
    this.projects.unshift(newProject);
    this.saveToStorage();
    return newProject;
  }
  updateProject(id: number | string, updates: Partial<MockProject>) {
    const idx = this.projects.findIndex(p => String(p.id) === String(id) || String(p.projectId) === String(id));
    if (idx !== -1) {
      this.projects[idx] = { ...this.projects[idx], ...updates };
      this.saveToStorage();
      return this.projects[idx];
    }
    return null;
  }
  deleteProject(id: number | string) {
    this.projects = this.projects.filter(p => String(p.id) !== String(id) && String(p.projectId) !== String(id));
    this.saveToStorage();
    return true;
  }

  // --- Releases ---
  getReleases(projectId?: number) {
    if (projectId) return this.releases.filter(r => Number(r.projectId) === Number(projectId));
    return [...this.releases];
  }
  getReleaseById(id: number | string) {
    return this.releases.find(r => String(r.id) === String(id) || String(r.releaseId) === String(id));
  }
  createRelease(releaseData: Partial<MockRelease>) {
    const newId = Date.now();
    const newRelease: MockRelease = {
      id: newId,
      releaseId: newId,
      name: releaseData.name || releaseData.releaseName || 'New Release',
      releaseName: releaseData.name || releaseData.releaseName || 'New Release',
      version: releaseData.version || releaseData.releaseVersion || 'v1.0.0',
      releaseVersion: releaseData.version || releaseData.releaseVersion || 'v1.0.0',
      description: releaseData.description || 'Release Description',
      projectId: Number(releaseData.projectId) || 1,
      projectName: releaseData.projectName || 'Project',
      status: releaseData.status || 'In Progress',
      releaseStatus: releaseData.releaseStatus || releaseData.status || 'In Progress',
      releaseTypeId: releaseData.releaseTypeId || 1,
      releaseTypeName: releaseData.releaseTypeName || 'Major Release',
      startDate: releaseData.startDate || new Date().toISOString().split('T')[0],
      releaseDate: releaseData.releaseDate || new Date(Date.now() + 30 * 24 * 3600 * 1000).toISOString().split('T')[0],
      endDate: releaseData.endDate || new Date(Date.now() + 30 * 24 * 3600 * 1000).toISOString().split('T')[0],
      kloc: releaseData.kloc ?? 20,
      totalTestCases: 0,
      passedTestCases: 0,
      failedTestCases: 0,
      blockedTestCases: 0,
      unexecutedTestCases: 0,
      features: releaseData.features || [],
      bugFixes: releaseData.bugFixes || [],
      testCases: [],
      createdAt: new Date().toISOString(),
      ...releaseData,
    };
    this.releases.unshift(newRelease);
    this.saveToStorage();
    return newRelease;
  }
  updateRelease(id: number | string, updates: Partial<MockRelease>) {
    const idx = this.releases.findIndex(r => String(r.id) === String(id) || String(r.releaseId) === String(id));
    if (idx !== -1) {
      this.releases[idx] = { ...this.releases[idx], ...updates };
      this.saveToStorage();
      return this.releases[idx];
    }
    return null;
  }
  deleteRelease(id: number | string) {
    this.releases = this.releases.filter(r => String(r.id) !== String(id) && String(r.releaseId) !== String(id));
    this.saveToStorage();
    return true;
  }

  // --- Modules & Submodules ---
  getModules(projectId?: number) {
    if (projectId) return this.modules.filter(m => Number(m.projectId) === Number(projectId));
    return [...this.modules];
  }
  getModuleById(id: number) { return this.modules.find(m => m.id === Number(id)); }
  createModule(moduleData: Partial<MockModule>) {
    const newId = Date.now();
    const newModule: MockModule = {
      id: newId,
      moduleId: newId,
      name: moduleData.name || moduleData.moduleName || 'New Module',
      moduleName: moduleData.name || moduleData.moduleName || 'New Module',
      projectId: Number(moduleData.projectId) || 1,
      description: moduleData.description || '',
      leaderId: moduleData.leaderId || null,
      leaderName: moduleData.leaderName || null,
      allocatedLeader: moduleData.leaderId ? {
        id: Date.now(),
        employeeId: moduleData.leaderId,
        employeeName: moduleData.leaderName || 'Leader',
        allocatedDate: new Date().toISOString().split('T')[0],
      } : null,
      assignedDevs: moduleData.assignedDevs || [],
      submodules: [],
      ...moduleData,
    };
    this.modules.push(newModule);
    this.saveToStorage();
    return newModule;
  }
  updateModule(id: number, updates: Partial<MockModule>) {
    const idx = this.modules.findIndex(m => m.id === Number(id));
    if (idx !== -1) {
      this.modules[idx] = { ...this.modules[idx], ...updates };
      this.saveToStorage();
      return this.modules[idx];
    }
    return null;
  }
  deleteModule(id: number) {
    this.modules = this.modules.filter(m => m.id !== Number(id));
    this.saveToStorage();
    return true;
  }

  // Submodules
  getSubmodulesByModule(moduleId: number) {
    const mod = this.getModuleById(moduleId);
    return mod?.submodules || [];
  }
  createSubmodule(moduleId: number, subData: any) {
    const mod = this.getModuleById(moduleId);
    if (mod) {
      const newSubId = Date.now();
      const newSub: MockSubmodule = {
        id: newSubId,
        subModuleId: newSubId,
        name: subData.name || subData.subModuleName || 'New Submodule',
        subModuleName: subData.name || subData.subModuleName || 'New Submodule',
        moduleId: Number(moduleId),
        description: subData.description || '',
        assignedDevs: [],
      };
      if (!mod.submodules) mod.submodules = [];
      mod.submodules.push(newSub);
      this.saveToStorage();
      return newSub;
    }
    return null;
  }
  updateSubmodule(moduleId: number, subModuleId: number, updates: any) {
    const mod = this.getModuleById(moduleId);
    if (mod && mod.submodules) {
      const idx = mod.submodules.findIndex(s => s.id === Number(subModuleId));
      if (idx !== -1) {
        mod.submodules[idx] = { ...mod.submodules[idx], ...updates };
        this.saveToStorage();
        return mod.submodules[idx];
      }
    }
    return null;
  }
  deleteSubmodule(moduleId: number, subModuleId: number) {
    const mod = this.getModuleById(moduleId);
    if (mod && mod.submodules) {
      mod.submodules = mod.submodules.filter(s => s.id !== Number(subModuleId));
      this.saveToStorage();
      return true;
    }
    return false;
  }

  // --- Test Cases ---
  getTestCases(subModuleId?: number) {
    if (subModuleId) return this.testCases.filter(t => Number(t.subModuleId) === Number(subModuleId));
    return [...this.testCases];
  }
  getTestCaseById(id: number) { return this.testCases.find(t => t.id === Number(id)); }
  createTestCase(testCaseData: Partial<MockTestCase>) {
    const newId = Date.now();
    const newTestCase: MockTestCase = {
      id: newId,
      testcaseNo: testCaseData.testcaseNo || `TC-${Math.floor(100 + Math.random() * 900)}`,
      no: testCaseData.testcaseNo || `TC-${Math.floor(100 + Math.random() * 900)}`,
      description: testCaseData.description || '',
      detailsSteps: testCaseData.detailsSteps || testCaseData.steps || '',
      steps: testCaseData.steps || testCaseData.detailsSteps || '',
      expectedResult: testCaseData.expectedResult || '',
      subModuleId: Number(testCaseData.subModuleId) || 1,
      subModuleName: testCaseData.subModuleName || 'Submodule',
      moduleId: Number(testCaseData.moduleId) || 1,
      moduleName: testCaseData.moduleName || 'Module',
      projectId: Number(testCaseData.projectId) || 1,
      severityId: Number(testCaseData.severityId) || 2,
      severityName: testCaseData.severityName || 'Medium',
      severity: testCaseData.severity || testCaseData.severityName || 'Medium',
      defectTypeId: Number(testCaseData.defectTypeId) || 2,
      defectTypeName: testCaseData.defectTypeName || 'Functional Bug',
      type: testCaseData.type || testCaseData.defectTypeName || 'Functional Bug',
      executionStatus: testCaseData.executionStatus || 'NOT_RUN',
      assignedQaId: testCaseData.assignedQaId || 1,
      assignedQaName: testCaseData.assignedQaName || 'Super Admin',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      createdBy: testCaseData.createdBy || 'Super Admin',
      updatedBy: testCaseData.updatedBy || 'Super Admin',
      ...testCaseData,
    };
    this.testCases.unshift(newTestCase);
    this.saveToStorage();
    return newTestCase;
  }
  updateTestCase(id: number, updates: Partial<MockTestCase>) {
    const idx = this.testCases.findIndex(t => t.id === Number(id));
    if (idx !== -1) {
      this.testCases[idx] = { ...this.testCases[idx], ...updates, updatedAt: new Date().toISOString() };
      this.saveToStorage();
      return this.testCases[idx];
    }
    return null;
  }
  deleteTestCase(id: number) {
    this.testCases = this.testCases.filter(t => t.id !== Number(id));
    this.saveToStorage();
    return true;
  }

  // --- Defects ---
  getDefects(projectId?: number) {
    if (projectId) return this.defects.filter(d => Number(d.projectId) === Number(projectId));
    return [...this.defects];
  }
  getDefectById(id: number) { return this.defects.find(d => d.id === Number(id)); }
  createDefect(defectData: Partial<MockDefect>) {
    const newId = Date.now();
    const newDefect: MockDefect = {
      id: newId,
      defectId: defectData.defectId || `DEF-${Math.floor(100 + Math.random() * 900)}`,
      title: defectData.title || defectData.description || 'New Defect',
      description: defectData.description || defectData.title || '',
      priority: (defectData.priority as any) || 'medium',
      priorityId: defectData.priorityId || 2,
      priorityName: defectData.priorityName || 'Medium',
      severity: (defectData.severity as any) || 'medium',
      severityId: defectData.severityId || 2,
      severityName: defectData.severityName || 'Medium',
      status: defectData.status || 'new',
      statusId: defectData.statusId || 1,
      statusName: defectData.statusName || 'New',
      defectStatusId: defectData.defectStatusId || 1,
      defectStatusName: defectData.defectStatusName || 'New',
      projectId: Number(defectData.projectId) || 1,
      projectName: defectData.projectName || 'Project',
      releaseId: defectData.releaseId || 1,
      releaseName: defectData.releaseName || 'Release 1.0',
      moduleId: defectData.moduleId || 1,
      moduleName: defectData.moduleName || 'Module',
      subModuleId: defectData.subModuleId || 1,
      subModuleName: defectData.subModuleName || 'Submodule',
      testCaseId: defectData.testCaseId || null,
      steps: defectData.steps || '',
      stepsToReproduce: defectData.stepsToReproduce || (defectData.steps ? [defectData.steps] : []),
      reOpenCount: 0,
      reopenCount: 0,
      attachment: defectData.attachment || null,
      attachments: defectData.attachments || [],
      reportedBy: defectData.reportedBy || 'Super Admin',
      assignedTo: defectData.assignedTo || 'Unassigned',
      assignedToId: defectData.assignedToId || null,
      assignedToName: defectData.assignedToName || 'Unassigned',
      assignedById: defectData.assignedById || 1,
      assignedByName: defectData.assignedByName || 'Super Admin',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      defectHistory: [
        {
          id: Date.now(),
          status: 'new',
          changedAt: new Date().toISOString(),
          comment: 'Defect created',
          changedBy: defectData.reportedBy || 'QA Tester',
        },
      ],
      comments: [],
      ...defectData,
    };
    this.defects.unshift(newDefect);
    this.saveToStorage();
    return newDefect;
  }
  updateDefect(id: number, updates: Partial<MockDefect>) {
    const idx = this.defects.findIndex(d => d.id === Number(id));
    if (idx !== -1) {
      const old = this.defects[idx];
      const updated = { ...old, ...updates, updatedAt: new Date().toISOString() };
      
      // If status changed, record in history
      if (updates.status && updates.status !== old.status) {
        if (!updated.defectHistory) updated.defectHistory = [];
        updated.defectHistory.push({
          id: Date.now(),
          status: updates.status,
          changedAt: new Date().toISOString(),
          comment: (updates as any).comment || `Status changed from ${old.status} to ${updates.status}`,
          changedBy: (updates as any).changedBy || 'System User',
        });
        if (updates.status.toLowerCase() === 'reopened') {
          updated.reOpenCount = (updated.reOpenCount || 0) + 1;
          updated.reopenCount = updated.reOpenCount;
        }
      }
      this.defects[idx] = updated;
      this.saveToStorage();
      return this.defects[idx];
    }
    return null;
  }
  deleteDefect(id: number) {
    this.defects = this.defects.filter(d => d.id !== Number(id));
    this.saveToStorage();
    return true;
  }
  addDefectComment(defectId: number, commentText: string, user?: MockUser) {
    const def = this.getDefectById(defectId);
    if (def) {
      if (!def.comments) def.comments = [];
      const newComment = {
        id: Date.now(),
        defectId: Number(defectId),
        userId: user?.id || 1,
        userName: user ? `${user.firstName} ${user.lastName}` : 'Super Admin',
        comment: commentText,
        createdAt: new Date().toISOString(),
      };
      def.comments.push(newComment);
      this.saveToStorage();
      return newComment;
    }
    return null;
  }

  // --- Bench ---
  getBenchResources() { return [...this.benchResources]; }
  allocateBenchResource(employeeId: number, projectId: number, allocationPercent: number) {
    const user = this.getUserById(employeeId);
    if (user) {
      user.availabilityPercent = Math.max(0, (user.availabilityPercent || 100) - allocationPercent);
      const proj = this.getProjectById(projectId);
      if (proj && !user.currentProjects?.includes(proj.name || '')) {
        user.currentProjects = [...(user.currentProjects || []), proj.name || ''];
      }
      this.saveToStorage();
      return true;
    }
    return false;
  }

  // --- Configurations ---
  getDesignations() { return [...this.designations]; }
  createDesignation(data: any) {
    const name = typeof data === 'string' ? data : (data.designationName || data.name || '');
    const newDesig = { id: Date.now(), designationName: name, name, description: data.description || '', totalEmployees: 0 };
    this.designations.push(newDesig);
    this.saveToStorage();
    return newDesig;
  }
  updateDesignation(id: number, data: any) {
    const idx = this.designations.findIndex(d => d.id === Number(id));
    if (idx !== -1) {
      const name = typeof data === 'string' ? data : (data.designationName || data.name || this.designations[idx].designationName);
      this.designations[idx] = { ...this.designations[idx], ...(typeof data === 'object' ? data : {}), designationName: name, name };
      this.saveToStorage();
      return this.designations[idx];
    }
    return null;
  }
  deleteDesignation(id: number) {
    this.designations = this.designations.filter(d => d.id !== Number(id));
    this.saveToStorage();
    return true;
  }

  getRoles() { return [...this.roles]; }
  createRole(data: any) {
    const name = typeof data === 'string' ? data : (data.roleName || data.name || '');
    const newRole = { id: Date.now(), roleName: name, name, description: data.description || '', totalPermissions: data.totalPermissions || 0 };
    this.roles.push(newRole);
    this.saveToStorage();
    return newRole;
  }
  updateRole(id: number, data: any) {
    const idx = this.roles.findIndex(r => r.id === Number(id));
    if (idx !== -1) {
      const name = typeof data === 'string' ? data : (data.roleName || data.name || this.roles[idx].roleName);
      this.roles[idx] = { ...this.roles[idx], ...(typeof data === 'object' ? data : {}), roleName: name, name };
      this.saveToStorage();
      return this.roles[idx];
    }
    return null;
  }
  deleteRole(id: number) {
    this.roles = this.roles.filter(r => r.id !== Number(id));
    this.saveToStorage();
    return true;
  }

  getPermissions() { return [...this.permissions]; }
  getSeverities() { return [...this.severities]; }
  createSeverity(data: any) {
    const name = data.name || data.severityName || '';
    const newSev = { id: Date.now(), name, severityName: name, color: data.color || '#3B82F6', weight: data.weight || 1, description: data.description || '' };
    this.severities.push(newSev);
    this.saveToStorage();
    return newSev;
  }
  updateSeverity(id: number, data: any) {
    const idx = this.severities.findIndex(s => s.id === Number(id));
    if (idx !== -1) {
      const name = data.name || data.severityName || this.severities[idx].name;
      this.severities[idx] = { ...this.severities[idx], ...data, name, severityName: name };
      this.saveToStorage();
      return this.severities[idx];
    }
    return null;
  }
  deleteSeverity(id: number) {
    this.severities = this.severities.filter(s => s.id !== Number(id));
    this.saveToStorage();
    return true;
  }

  getPriorities() { return [...this.priorities]; }
  createPriority(data: any) {
    const name = data.name || data.priorityName || '';
    const newPrio = { id: Date.now(), name, priorityName: name, color: data.color || '#3B82F6', description: data.description || '' };
    this.priorities.push(newPrio);
    this.saveToStorage();
    return newPrio;
  }
  updatePriority(id: number, data: any) {
    const idx = this.priorities.findIndex(p => p.id === Number(id));
    if (idx !== -1) {
      const name = data.name || data.priorityName || this.priorities[idx].name;
      this.priorities[idx] = { ...this.priorities[idx], ...data, name, priorityName: name };
      this.saveToStorage();
      return this.priorities[idx];
    }
    return null;
  }
  deletePriority(id: number) {
    this.priorities = this.priorities.filter(p => p.id !== Number(id));
    this.saveToStorage();
    return true;
  }

  getDefectTypes() { return [...this.defectTypes]; }
  createDefectType(data: any) {
    const name = typeof data === 'string' ? data : (data.defectTypeName || data.name || '');
    const newDT = { id: Date.now(), defectTypeName: name, name, description: data.description || name };
    this.defectTypes.push(newDT);
    this.saveToStorage();
    return newDT;
  }
  updateDefectType(id: number, data: any) {
    const idx = this.defectTypes.findIndex(d => d.id === Number(id));
    if (idx !== -1) {
      const name = typeof data === 'string' ? data : (data.defectTypeName || data.name || this.defectTypes[idx].defectTypeName);
      this.defectTypes[idx] = { ...this.defectTypes[idx], ...(typeof data === 'object' ? data : {}), defectTypeName: name, name };
      this.saveToStorage();
      return this.defectTypes[idx];
    }
    return null;
  }
  deleteDefectType(id: number) {
    this.defectTypes = this.defectTypes.filter(d => d.id !== Number(id));
    this.saveToStorage();
    return true;
  }

  getReleaseTypes() { return [...this.releaseTypes]; }
  createReleaseType(data: any) {
    const name = typeof data === 'string' ? data : (data.releaseTypeName || data.name || '');
    const newRT = { id: Date.now(), releaseTypeName: name, name };
    this.releaseTypes.push(newRT);
    this.saveToStorage();
    return newRT;
  }
  updateReleaseType(id: number, data: any) {
    const idx = this.releaseTypes.findIndex(r => r.id === Number(id));
    if (idx !== -1) {
      const name = typeof data === 'string' ? data : (data.releaseTypeName || data.name || this.releaseTypes[idx].releaseTypeName);
      this.releaseTypes[idx] = { ...this.releaseTypes[idx], ...(typeof data === 'object' ? data : {}), releaseTypeName: name, name };
      this.saveToStorage();
      return this.releaseTypes[idx];
    }
    return null;
  }
  deleteReleaseType(id: number) {
    this.releaseTypes = this.releaseTypes.filter(r => r.id !== Number(id));
    this.saveToStorage();
    return true;
  }

  getStatusTypes() { return [...this.statusTypes]; }
  getStatuses() { return [...this.statusTypes]; }
  getDefectStatuses() { return [...this.statusTypes]; }
  createStatusType(data: any) {
    const name = data.name || data.statusName || '';
    const newST = { id: Date.now(), name, statusName: name, color: data.color || '#3B82F6', isInitial: data.isInitial || false, description: data.description || '' };
    this.statusTypes.push(newST);
    this.saveToStorage();
    return newST;
  }
  createStatus(data: any) {
    return this.createStatusType(data);
  }
  updateStatusType(id: number, data: any) {
    const idx = this.statusTypes.findIndex(s => s.id === Number(id));
    if (idx !== -1) {
      const name = data.name || data.statusName || this.statusTypes[idx].name;
      this.statusTypes[idx] = { ...this.statusTypes[idx], ...data, name, statusName: name };
      this.saveToStorage();
      return this.statusTypes[idx];
    }
    return null;
  }
  updateStatus(id: number, data: any) {
    return this.updateStatusType(id, data);
  }
  deleteStatusType(id: number) {
    this.statusTypes = this.statusTypes.filter(s => s.id !== Number(id));
    this.saveToStorage();
    return true;
  }
  deleteStatus(id: number) {
    return this.deleteStatusType(id);
  }

  getEmailConfigs() { return [...this.emailConfigs]; }
  createEmailConfig(data: any) {
    const newEC = { id: Date.now(), ...data, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() };
    this.emailConfigs.push(newEC);
    this.saveToStorage();
    return newEC;
  }
  updateEmailConfig(id: number, data: any) {
    const idx = this.emailConfigs.findIndex(e => e.id === Number(id));
    if (idx !== -1) {
      this.emailConfigs[idx] = { ...this.emailConfigs[idx], ...data, updatedAt: new Date().toISOString() };
      this.saveToStorage();
      return this.emailConfigs[idx];
    }
    return null;
  }
  deleteEmailConfig(id: number) {
    this.emailConfigs = this.emailConfigs.filter(e => e.id !== Number(id));
    this.saveToStorage();
    return true;
  }

  getEmailPointSetups() { return [...this.emailPointSetups]; }
  getEmailTemplates() { return [...this.emailTemplates]; }
}

export const mockDb = new MockDbStore();
