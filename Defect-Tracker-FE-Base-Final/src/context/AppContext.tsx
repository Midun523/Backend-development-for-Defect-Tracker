import React, { createContext, useContext, useState, useEffect } from "react";
import {
  Employee,
  Project,
  Defect,
  TestCase,
  Release,
  WorkflowItem,
  BenchAllocation,
  WorkflowStatus,
  StatusTransition
} from "../types/index";
import { getModulesByProjectId } from "../api/module/getModule";
import { getAllDefectStatuses } from "../api/defectStatus";
import { tokenManager } from "../lib/api";

interface Submodule {
  id: string;
  name: string;
  assignedDevs: string[];
}

interface Module {
  id: string;
  name: string;
  submodules: Submodule[];
  assignedDevs: string[];
}

interface ModulesByProject {
  [projectId: string]: Module[];
}


interface StatusType {
  id: string;
  name: string;
  color: string;
}

interface AppContextType {
  employees: Employee[];
  projects: Project[];
  defects: Defect[];
  testCases: TestCase[];
  releases: Release[];
  workflowItems: WorkflowItem[];
  benchAllocations: BenchAllocation[];
  workflowStatuses: WorkflowStatus[];
  transitions: StatusTransition[];
  statusTypes: StatusType[];
  setStatusTypes: React.Dispatch<React.SetStateAction<StatusType[]>>;
  selectedProjectId: string | null;
  setSelectedProjectId: (id: string | null) => void;
  addEmployee: (
    employee: Omit<Employee, "id" | "createdAt" | "updatedAt">
  ) => void;
  updateEmployee: (id: string, employee: Partial<Employee>) => void;
  deleteEmployee: (id: string) => void;
  addProject: (project: Project) => void;
  updateProject: (project: Project) => void;
  deleteProject: (projectId: string) => void;
  addDefect: (defect: Defect) => void;
  updateDefect: (defect: Defect) => void;
  deleteDefect: (defectId: string) => void;
  addTestCase: (testCase: TestCase) => void;
  updateTestCase: (testCase: TestCase) => void;
  deleteTestCase: (testCaseId: string) => void;
  addRelease: (release: Release) => void;
  updateRelease: (release: Release) => void;
  deleteRelease: (releaseId: string) => void;
  updateWorkflowItem: (id: string, updates: Partial<WorkflowItem>) => void;
  moveTestCaseToRelease: (testCaseIds: string[], releaseId: string) => void;
  allocateEmployee: (
    allocation: Omit<BenchAllocation, "id" | "createdAt">
  ) => void;
  updateWorkflowStatuses: (statuses: WorkflowStatus[]) => void;
  updateTransitions: (transitions: StatusTransition[]) => void;
  addStatusType: (statusType: Omit<StatusType, "id">) => void;
  updateStatusType: (id: string, statusType: Partial<StatusType>) => void;
  deleteStatusType: (id: string) => void;
  testCaseDefectMap: { [testCaseId: string]: string };
  setTestCaseDefectMap: React.Dispatch<
    React.SetStateAction<{ [testCaseId: string]: string }>
  >;
  modulesByProject: ModulesByProject;
  setModulesByProject: React.Dispatch<React.SetStateAction<ModulesByProject>>;
  addModule: (projectId: string, module: Module) => void;
  updateModule: (
    projectId: string,
    moduleId: string,
    updated: Partial<Module>
  ) => void;
  deleteModule: (projectId: string, moduleId: string) => void;
  addSubmodule: (
    projectId: string,
    moduleId: string,
    submodule: Submodule
  ) => void;
  updateSubmodule: (
    projectId: string,
    moduleId: string,
    submoduleIdx: number,
    newName: string
  ) => void;
  deleteSubmodule: (
    projectId: string,
    moduleId: string,
    submoduleIdx: number
  ) => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const useApp = () => {
  const context = useContext(AppContext);
  if (context === undefined) {
    throw new Error("useApp must be used within AppProvider");
  }
  return context;
};

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [defects, setDefects] = useState<Defect[]>([]);
  const [testCases, setTestCases] = useState<TestCase[]>([]);
  const [releases, setReleases] = useState<Release[]>([]);

  const [workflowItems, setWorkflowItems] = useState<WorkflowItem[]>([]);
  const [benchAllocations, setBenchAllocations] = useState<BenchAllocation[]>(
    []
  );
  const [workflowStatuses, setWorkflowStatuses] = useState<WorkflowStatus[]>(
    []
  );
  const [transitions, setTransitions] = useState<StatusTransition[]>([]);
  const [statusTypes, setStatusTypes] = useState<StatusType[]>([]);
  const [selectedProjectId, setSelectedProjectIdState] = useState<string | null>(() => {
    return localStorage.getItem('selectedProjectId') || null;
  });

  const setSelectedProjectId = (id: string | null) => {
    setSelectedProjectIdState(id);
    if (id) {
      localStorage.setItem('selectedProjectId', id);
    } else {
      localStorage.removeItem('selectedProjectId');
    }
  };
  const [testCaseDefectMap, setTestCaseDefectMap] = useState<{
    [testCaseId: string]: string;
  }>({});

  const [modulesByProject, setModulesByProject] = useState<ModulesByProject>({});

  
  const addModule = (projectId: string, module: Module) => {
    setModulesByProject((prev) => ({
      ...prev,
      [projectId]: prev[projectId] ? [...prev[projectId], module] : [module],
    }));
  };

  const updateModule = (
    projectId: string,
    moduleId: string,
    updated: Partial<Module>
  ) => {
    setModulesByProject((prev) => ({
      ...prev,
      [projectId]:
        prev[projectId]?.map((m) =>
          m.id === moduleId ? { ...m, ...updated } : m
        ) || [],
    }));
  };

  const deleteModule = (projectId: string, moduleId: string) => {
    setModulesByProject((prev) => ({
      ...prev,
      [projectId]: prev[projectId]?.filter((m) => m.id !== moduleId) || [],
    }));
  };

  const addSubmodule = (
    projectId: string,
    moduleId: string,
    submodule: Submodule
  ) => {
    setModulesByProject((prev) => ({
      ...prev,
      [projectId]:
        prev[projectId]?.map((m) =>
          m.id === moduleId
            ? { ...m, submodules: [...m.submodules, submodule] }
            : m
        ) || [],
    }));
  };

  const updateSubmodule = (
    projectId: string,
    moduleId: string,
    submoduleIdx: number,
    newName: string
  ) => {
    setModulesByProject((prev) => ({
      ...prev,
      [projectId]:
        prev[projectId]?.map((m) =>
          m.id === moduleId
            ? {
              ...m,
              submodules: m.submodules.map((s, i) =>
                i === submoduleIdx ? { ...s, name: newName } : s
              ),
            }
            : m
        ) || [],
    }));
  };

  const deleteSubmodule = (
    projectId: string,
    moduleId: string,
    submoduleIdx: number
  ) => {
    setModulesByProject((prev) => ({
      ...prev,
      [projectId]:
        prev[projectId]?.map((m) =>
          m.id === moduleId
            ? {
              ...m,
              submodules: m.submodules.filter((_, i) => i !== submoduleIdx),
            }
            : m
        ) || [],
    }));
  };

  const addEmployee = (
    employeeData: Omit<Employee, "id" | "createdAt" | "updatedAt">
  ) => {
    const newEmployee: Employee = {
      ...employeeData,
      id: Date.now().toString(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    setEmployees((prev) => [...prev, newEmployee]);
  };

  const updateEmployee = (id: string, employeeData: Partial<Employee>) => {
    setEmployees((prev) =>
      prev.map((emp) =>
        emp.id === id
          ? { ...emp, ...employeeData, updatedAt: new Date().toISOString() }
          : emp
      )
    );
  };

  const deleteEmployee = (id: string) => {
    setEmployees((prev) => prev.filter((emp) => emp.id !== id));
  };

  const addProject = (project: Project) => {
    setProjects((prev) => [project, ...prev]);
  };

  const updateProject = (project: Project) => {
    setProjects((prev) => prev.map((p) => (p.id === project.id ? project : p)));
  };

  const deleteProject = (projectId: string) => {
    setProjects((prev) => prev.filter((p) => p.id !== projectId));
  };

  const addDefect = (defect: Defect) => {
    setDefects((prev) => [...prev, defect]);
  };

  const updateDefect = (defect: Defect) => {
    setDefects((prev) => prev.map((d) => (d.id === defect.id ? defect : d)));
  };

  const deleteDefect = (defectId: string) => {
    setDefects((prev) => prev.filter((d) => d.id !== defectId));
  };

  const addTestCase = (testCase: TestCase) => {
    setTestCases((prev) => [...prev, testCase]);
  };

  const updateTestCase = (testCase: TestCase) => {
    setTestCases((prev) =>
      prev.map((tc) => (tc.id === testCase.id ? testCase : tc))
    );
  };

  const deleteTestCase = (testCaseId: string) => {
    setTestCases((prev) => prev.filter((tc) => tc.id !== testCaseId));
  };

  const addRelease = (release: Release) => {
    
    setReleases((prev) => [...prev, release]);
  };

  const updateRelease = (release: Release) => {
    
  };

  const deleteRelease = (releaseId: string) => {
    
  };

  const updateWorkflowItem = (id: string, updates: Partial<WorkflowItem>) => {
    setWorkflowItems((prev) =>
      prev.map((item) => (item.id === id ? { ...item, ...updates } : item))
    );
  };

  const moveTestCaseToRelease = (testCaseIds: string[], releaseId: string) => {
    
  };

  const allocateEmployee = (
    allocationData: Omit<BenchAllocation, "id" | "createdAt">
  ) => {
    
  };

  const updateWorkflowStatuses = (statuses: WorkflowStatus[]) => {
    setWorkflowStatuses(statuses);
  };

  const updateTransitions = (newTransitions: StatusTransition[]) => {
    setTransitions(newTransitions);
  };

  const addStatusType = (statusTypeData: Omit<StatusType, "id">) => {
    const newStatusType: StatusType = {
      ...statusTypeData,
      id: Date.now().toString(),
    };
    setStatusTypes((prev) => [...prev, newStatusType]);
  };

  const updateStatusType = (
    id: string,
    statusTypeData: Partial<StatusType>
  ) => {
    setStatusTypes((prev) =>
      prev.map((status) =>
        status.id === id ? { ...status, ...statusTypeData } : status
      )
    );
  };

  const deleteStatusType = (id: string) => {
    setStatusTypes((prev) => prev.filter((status) => status.id !== id));
  };

  
  useEffect(() => {
    const fetchStatusTypes = async () => {
      try {
        const token = tokenManager.getToken();
        if (!token) {
          return;
        }

        const response = await getAllDefectStatuses();

        
        const apiStatusTypes = response.content.map((status: any) => ({
          id: String(status.id),
          name: status.name,
          color: status.color
        }));

        setStatusTypes(apiStatusTypes);
      } catch (error) {
        console.error('Failed to fetch status types:', error);
      }
    };

    fetchStatusTypes();

    
    const handleRefreshStatusTypes = (event: CustomEvent) => {
      const { statusTypes: refreshedStatusTypes } = event.detail;
      if (refreshedStatusTypes) {
        setStatusTypes(refreshedStatusTypes);
      }
    };

    window.addEventListener('refreshDefectStatuses', handleRefreshStatusTypes as EventListener);

    
    return () => {
      window.removeEventListener('refreshDefectStatuses', handleRefreshStatusTypes as EventListener);
    };
  }, []);

  useEffect(() => {
    if (!selectedProjectId) return;
    
    
    if (modulesByProject[selectedProjectId] && modulesByProject[selectedProjectId].length > 0) {
      console.log('AppContext: Using initial mock modules for project:', selectedProjectId);
      return;
    }

    getModulesByProjectId(selectedProjectId)
      .then((res) => {
        const modules = (res?.data || []).map((mod: any) => ({
          id: String(mod.id),
          name: mod.moduleName || mod.name,
          assignedDevs: [],
          submodules: (mod.submodules || []).map((sm: any) => ({
            id: String(sm.id),
            name: sm.subModuleName || sm.name,
            assignedDevs: [],
          })),
        }));
        if (modules.length > 0) {
          setModulesByProject((prev) => ({ ...prev, [selectedProjectId]: modules }));
        }
      })
      .catch(error => {
        console.error('Failed to fetch modules for project:', error.message);
        
      });
  }, [selectedProjectId, modulesByProject]);

  return (
    <AppContext.Provider
      value={{
        employees,
        projects,
        defects,
        testCases,
        releases,
        workflowItems,
        benchAllocations,
        workflowStatuses,
        transitions,
        statusTypes,
setStatusTypes,
selectedProjectId,
        setSelectedProjectId,
        addEmployee,
        updateEmployee,
        deleteEmployee,
        addProject,
        updateProject,
        deleteProject,
        addDefect,
        updateDefect,
        deleteDefect,
        addTestCase,
        updateTestCase,
        deleteTestCase,
        addRelease,
        updateRelease,
        deleteRelease,
        updateWorkflowItem,
        moveTestCaseToRelease,
        allocateEmployee,
        updateWorkflowStatuses,
        updateTransitions,
        addStatusType,
        updateStatusType,
        deleteStatusType,
        testCaseDefectMap,
        setTestCaseDefectMap,
        modulesByProject,
        setModulesByProject,
        addModule,
        updateModule,
        deleteModule,
        addSubmodule,
        updateSubmodule,
        deleteSubmodule,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};