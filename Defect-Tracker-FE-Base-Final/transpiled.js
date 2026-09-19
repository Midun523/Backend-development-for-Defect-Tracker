import { createHotContext as __vite__createHotContext } from "/@vite/client";import.meta.hot = __vite__createHotContext("/src/pages/TestExecution.tsx");import __vite__cjsImport0_react_jsxDevRuntime from "/node_modules/.vite/deps/react_jsx-dev-runtime.js?v=b01a6504"; const Fragment = __vite__cjsImport0_react_jsxDevRuntime["Fragment"]; const jsxDEV = __vite__cjsImport0_react_jsxDevRuntime["jsxDEV"];
import * as RefreshRuntime from "/@react-refresh";
const inWebWorker = typeof WorkerGlobalScope !== "undefined" && self instanceof WorkerGlobalScope;
let prevRefreshReg;
let prevRefreshSig;
if (import.meta.hot && !inWebWorker) {
  if (!window.$RefreshReg$) {
    throw new Error(
      "@vitejs/plugin-react can't detect preamble. Something is wrong."
    );
  }
  prevRefreshReg = window.$RefreshReg$;
  prevRefreshSig = window.$RefreshSig$;
  window.$RefreshReg$ = RefreshRuntime.getRefreshReg("C:/Users/MIDUN/Desktop/Defect Tracker Backend/Defect-Tracker-FE-Base-Final/src/pages/TestExecution.tsx");
  window.$RefreshSig$ = RefreshRuntime.createSignatureFunctionForTransform;
}
var _s = $RefreshSig$();
import __vite__cjsImport3_react from "/node_modules/.vite/deps/react.js?v=b01a6504"; const useState = __vite__cjsImport3_react["useState"]; const useEffect = __vite__cjsImport3_react["useEffect"]; const useRef = __vite__cjsImport3_react["useRef"];
import { useNavigate, useParams } from "/node_modules/.vite/deps/react-router-dom.js?v=b01a6504";
import { Card, CardContent } from "/src/components/ui/Card.tsx";
import { Button } from "/src/components/ui/Button.tsx";
import { usePermission } from "/src/context/PermissionContext.tsx";
import {
  ChevronLeft,
  Eye,
  ChevronRight,
  FileText,
  Calendar
} from "/node_modules/lucide-react/dist/esm/lucide-react.js?v=b01a6504";
import { useApp } from "/src/context/AppContext.tsx";
import { Modal } from "/src/components/ui/Modal.tsx";
import { ProjectSelector } from "/src/components/ui/ProjectSelector.tsx";
import { projectReleaseCardView, getReleaseTestCaseCountsLoad } from "/src/api/releaseView/ProjectReleaseCardView.ts";
import { getAllSubmoduleAllocatedDevBySubmoduleId } from "/src/api/subModuleDevAlloc.ts";
import { getDevelopersWithRolesByProjectId } from "/src/api/bench/projectAllocation.ts";
import AuthService from "/src/services/authService.ts";
import { Toast } from "/src/components/ui/Toast.tsx";
import { updateReleaseStatus } from "/src/api/projectAllocationHistory/updateReleaseStatus.ts";
import { getModulesByProject } from "/src/api/module/getModuleByProject.ts";
import { getSubmodulesByModule } from "/src/api/submodule/getSubmodulesByModule.ts";
import { getTestCasesByFilter } from "/src/api/testExecution/getTestCasesByFilter.ts";
import { ImagePicker } from "/src/components/ui/ImagePicker.tsx";
import {
  getExecutionStatuses as getSavedExecutionStatuses,
  setExecutionStatus as persistExecutionStatus,
  updateReleaseTestCaseStatus,
  updateReleaseTestCaseStatusWithImage
} from "/src/api/testExecution/testExecution.ts";
import { getSeverities } from "/src/api/severity.ts";
import { getDefectTypes } from "/src/api/defectType.ts";
import { getAllPriorities } from "/src/api/priority.ts";
import { getAllDefectStatuses } from "/src/api/defectStatus.ts";
import {
  getUsersByModuleSubmoduleAllocation
} from "/src/api/module/getUsersByAllocation.ts";
import {
  filterDefectsForTest
} from "/src/api/defect/filterDefectByProject.ts";
import { getDefectTestCaseCounts } from "/src/api/releasetestcase.ts";
import { useAccessibleProjects } from "/src/api/useAccessibleProjects.ts";
const mockQA = [];
const mockTestCases = [];
const DEV_ROLE_TYPES = [
  "DEV_LEAD",
  "SENIOR_DEVELOPER",
  "DEVELOPER",
  "JUNIOR_DEVELOPER"
];
const mockReleases = [];
function useMockOrApiData(apiData, mockData) {
  if (!apiData || Array.isArray(apiData) && apiData.length === 0) {
    return mockData;
  }
  return apiData;
}
export const TestExecution = () => {
  _s();
  const { projectId, releaseId } = useParams();
  const navigate = useNavigate();
  const {
    releases,
    testCases,
    setSelectedProjectId,
    addDefect,
    testCaseDefectMap,
    setTestCaseDefectMap,
    defects,
    modulesByProject
  } = useApp();
  const { projects, switchProject } = useAccessibleProjects();
  const [selectedProject, setSelectedProject] = useState(
    projectId || null
  );
  const [selectedRelease, setSelectedRelease] = useState(
    releaseId || null
  );
  const [defectAllocatedUsers, setDefectAllocatedUsers] = useState(
    []
  );
  const [defectAllocatedUsersLoading, setDefectAllocatedUsersLoading] = useState(false);
  const [selectedModule, setSelectedModule] = useState("");
  const [selectedSubmodule, setSelectedSubmodule] = useState("");
  const [isViewStepsModalOpen, setIsViewStepsModalOpen] = useState(false);
  const [isViewTestCaseModalOpen, setIsViewTestCaseModalOpen] = useState(false);
  const [viewingTestCase, setViewingTestCase] = useState(null);
  console.log({ viewingTestCase });
  const [executionStatuses, setExecutionStatuses] = useState({});
  const [defectModalOpen, setDefectModalOpen] = useState(null);
  const { can } = usePermission();
  const [defectFormData, setDefectFormData] = useState({
    title: "",
    description: "",
    module: "",
    subModule: "",
    type: "",
    priority: "",
    severity: "",
    status: "",
    projectId: projectId || "",
    releaseId: releaseId || "",
    testCaseId: "",
    assignedTo: 0,
    reportedBy: "",
    rejectionComment: "",
    attachment: "",
    attachmentFile: null
  });
  const [releaseLoading, setReleaseLoading] = useState(false);
  const [releaseError, setReleaseError] = useState("");
  const [projectReleaseCard, setProjectReleaseCard] = useState([]);
  const [toast, setToast] = useState({
    isOpen: false,
    message: "",
    type: "success"
  });
  const [modules, setModules] = useState([]);
  const [modulesLoading, setModulesLoading] = useState(false);
  const [modulesError, setModulesError] = useState("");
  const [submodules, setSubmodules] = useState([]);
  const [submodulesLoading, setSubmodulesLoading] = useState(false);
  const [submodulesError, setSubmodulesError] = useState("");
  const [releaseTestCaseCounts, setReleaseTestCaseCounts] = useState({});
  const [releaseTestCaseCountsLoading, setReleaseTestCaseCountsLoading] = useState(false);
  const [severities, setSeverities] = useState([]);
  const [defectTypes, setDefectTypes] = useState(
    []
  );
  const [priorities, setPriorities] = useState(
    []
  );
  const [defectStatuses, setDefectStatuses] = useState(
    []
  );
  const [allocatedUsers, setAllocatedUsers] = useState(
    []
  );
  const [allocatedUsersLoading, setAllocatedUsersLoading] = useState(false);
  const [defectSubmitting, setDefectSubmitting] = useState(false);
  const [existingDefects, setExistingDefects] = useState([]);
  const [defectsLoading, setDefectsLoading] = useState(false);
  const [pendingStatusUpdate, setPendingStatusUpdate] = useState(null);
  const [previousStatusBeforeFail, setPreviousStatusBeforeFail] = useState(null);
  const [editingDefect, setEditingDefect] = useState(null);
  const [isEditingDefect, setIsEditingDefect] = useState(false);
  const currentUser = AuthService.getCurrentUser();
  const currentUserId = currentUser?.userId || currentUser?.employeeId || null;
  console.log("Current User ID:", currentUserId);
  const canUserExecute = (testCase) => {
    if (!currentUserId) {
      console.log("No current user ID found");
      return false;
    }
    if (!testCase.assignedToId) {
      console.log("Test case not assigned to anyone");
      return false;
    }
    const canExecute = Number(currentUserId) === Number(testCase.assignedToId);
    console.log(`User ${currentUserId} vs Assigned ${testCase.assignedToId}: ${canExecute}`);
    return canExecute;
  };
  useEffect(() => {
    console.log("Pending status update changed:", pendingStatusUpdate);
  }, [pendingStatusUpdate]);
  const [defectAssignments, setDefectAssignments] = useState({});
  const allocatedTestCasesMap = JSON.parse(
    localStorage.getItem("qaAllocatedTestCases") || "{}"
  );
  const qaAllocationsMap = JSON.parse(
    localStorage.getItem("qaAllocations") || "{}"
  );
  const submoduleHandledRef = useRef(false);
  const skipEffectUntilRef = useRef(0);
  const [localTestCaseDefectMap, setLocalTestCaseDefectMap] = useState({});
  function getAssignedQAForRelease(testCaseId) {
    const allocations = qaAllocationsMap[selectedRelease || ""] || {};
    for (const [qaId, ids] of Object.entries(allocations)) {
      if (ids.includes(testCaseId)) {
        const qa = effectiveQA && effectiveQA.find((q) => q.id === qaId);
        return qa ? qa.name : qaId;
      }
    }
    return null;
  }
  function getAssignedUserForDefect(defectId) {
    return defectAssignments[defectId] || null;
  }
  function normalize(text) {
    return (text || "").toString().toLowerCase().replace(/\s+/g, " ").trim();
  }
  function extractFieldValue(obj, fieldNames) {
    for (const fieldName of fieldNames) {
      if (obj[fieldName] !== void 0 && obj[fieldName] !== null && obj[fieldName] !== "") {
        return obj[fieldName];
      }
    }
    return "";
  }
  function findMatchingDefectForTestCase(testCase) {
    if (!existingDefects || existingDefects.length === 0 || !testCase)
      return null;
    const tcModule = normalize(
      extractFieldValue(testCase, ["module", "moduleName", "module_name"])
    );
    const tcSubmodule = normalize(
      extractFieldValue(
        testCase,
        [
          "subModule",
          "subModuleName",
          "sub_module_name"
        ]
      )
    );
    const tcDesc = normalize(
      extractFieldValue(
        testCase,
        [
          "description",
          "release_test_case_description"
        ]
      )
    );
    console.log("Looking for defect match for test case:", {
      id: testCase.id,
      module: tcModule,
      submodule: tcSubmodule,
      description: tcDesc,
      rawTestCase: testCase
    });
    const match = existingDefects.find((defect) => {
      const dModule = normalize(
        extractFieldValue(defect, ["module_name", "moduleName", "module"])
      );
      const dSubmodule = normalize(
        extractFieldValue(
          defect,
          [
            "sub_module_name",
            "subModuleName",
            "subModule"
          ]
        )
      );
      const dDesc = normalize(
        extractFieldValue(
          defect,
          [
            "release_test_case_description",
            "description",
            "steps"
          ]
        )
      );
      const descMatches = dDesc === tcDesc || dDesc && tcDesc && (dDesc.includes(tcDesc) || tcDesc.includes(dDesc) || dDesc.toLowerCase().includes(tcDesc.toLowerCase()) || tcDesc.toLowerCase().includes(dDesc.toLowerCase()));
      const moduleMatch = dModule === tcModule;
      const submoduleMatch = dSubmodule === tcSubmodule;
      console.log("Defect match attempt:", {
        defectId: defect.defectId || defect.id,
        dModule,
        dSubmodule,
        dDesc,
        moduleMatch,
        submoduleMatch,
        descMatches,
        rawDefect: defect
      });
      return moduleMatch && submoduleMatch && descMatches;
    });
    if (match) {
      console.log("Found matching defect:", match.defectId || match.id);
    } else {
      console.log("No matching defect found for test case:", testCase.id);
    }
    return match || null;
  }
  const safeProjectId = selectedProject || projectId || "";
  useEffect(() => {
    if (selectedProject) {
      setModulesLoading(true);
      setModulesError("");
      getModulesByProject(Number(selectedProject)).then((data) => setModules(data.data)).catch((err) => {
        setModulesError(err.message);
        setModules([]);
      }).finally(() => setModulesLoading(false));
    } else {
      setModules([]);
    }
  }, [selectedProject]);
  let storedMockTestCases = null;
  let storedMockQA = null;
  try {
    const stored = localStorage.getItem("mockTestCases");
    if (stored) storedMockTestCases = JSON.parse(stored);
    const storedQA = localStorage.getItem("mockQA");
    if (storedQA) storedMockQA = JSON.parse(storedQA);
  } catch (e) {
  }
  const effectiveTestCases = useMockOrApiData(
    testCases,
    storedMockTestCases || mockTestCases
  );
  const effectiveQA = storedMockQA || mockQA;
  const testCaseIdToQA = {};
  if (qaAllocationsMap && Array.isArray(qaAllocationsMap.allocations)) {
    qaAllocationsMap.allocations.forEach((alloc) => {
      alloc.testCaseIds.forEach((tcId) => {
        const qa = effectiveQA && effectiveQA.find(
          (q) => q.id === alloc.qaId || q.name === alloc.qaName
        );
        testCaseIdToQA[tcId] = qa ? qa.name : alloc.qaName || alloc.qaId;
      });
    });
  }
  const [filteredTestCases, setFilteredTestCases] = useState([]);
  useEffect(() => {
    if (selectedProject && selectedRelease) {
      const saved = getSavedExecutionStatuses(selectedProject, selectedRelease);
      setExecutionStatuses(
        saved
      );
    } else {
      setExecutionStatuses({});
    }
  }, [selectedProject, selectedRelease]);
  const [moduleTestCaseCounts, setModuleTestCaseCounts] = useState({});
  const [submoduleTestCaseCounts, setSubmoduleTestCaseCounts] = useState({});
  const [countsLoading, setCountsLoading] = useState(false);
  const [testCasesLoading, setTestCasesLoading] = useState(false);
  useEffect(() => {
    const fetchModuleTestCaseCounts = async () => {
      if (selectedProject && selectedRelease && modules.length > 0) {
        setCountsLoading(true);
        const counts = {};
        try {
          for (const module of modules) {
            const testCases2 = await getTestCasesByFilter({
              projectId: parseInt(selectedProject),
              releaseId: parseInt(selectedRelease),
              moduleId: module.id
            });
            console.log(`Module ${module.moduleName} test cases:`, testCases2);
            counts[module.moduleName || module.name] = testCases2.length;
          }
          setModuleTestCaseCounts(counts);
        } catch (err) {
          console.error("Failed to fetch module test case counts:", err);
          setModuleTestCaseCounts({});
        } finally {
          setCountsLoading(false);
        }
      }
    };
    fetchModuleTestCaseCounts();
  }, [selectedProject, selectedRelease, modules]);
  useEffect(() => {
    const fetchSubmoduleTestCaseCounts = async () => {
      if (selectedProject && selectedRelease && selectedModule && submodules.length > 0) {
        const counts = {};
        try {
          for (const submodule of submodules) {
            const testCases2 = await getTestCasesByFilter({
              projectId: parseInt(selectedProject),
              releaseId: parseInt(selectedRelease),
              moduleId: modules.find((m) => m.moduleName === selectedModule || m.name === selectedModule)?.id,
              subModuleId: submodule.id
            });
            console.log(
              `Submodule ${submodule.subModuleName || submodule.name} test cases:`,
              testCases2
            );
            counts[submodule.subModuleName || submodule.name] = testCases2.length;
          }
          setSubmoduleTestCaseCounts(counts);
        } catch (err) {
          console.error("Failed to fetch submodule test case counts:", err);
          setSubmoduleTestCaseCounts({});
        }
      }
    };
    fetchSubmoduleTestCaseCounts();
  }, [selectedProject, selectedRelease, selectedModule, submodules, modules]);
  useEffect(() => {
    if (selectedProject) {
      setSelectedProjectId(selectedProject);
    }
  }, [selectedProject, setSelectedProjectId]);
  useEffect(() => {
    if (selectedProject) {
      setReleaseLoading(true);
      setReleaseError("");
      setReleaseTestCaseCounts({});
      projectReleaseCardView(selectedProject).then((releasesRes) => {
        if (releasesRes.status === "Success" || releasesRes.statusCode === "200") {
          const releaseList = releasesRes.data || [];
          const filtered = releaseList.filter(
            (r) => String(r.project_id) === String(selectedProject) || String(r.projectId) === String(selectedProject)
          );
          setProjectReleaseCard(filtered);
          const releaseIds = filtered.map((r) => r.id || r.releaseId).filter((id) => id != null).map(Number);
          if (releaseIds.length > 0) {
            getReleaseTestCaseCountsLoad(releaseIds).then((countsRes) => {
              if (countsRes.status === "Success" || countsRes.statusCode === 200) {
                const countsMap = {};
                (countsRes.data || []).forEach((item) => {
                  const releaseId2 = item.releaseId;
                  if (releaseId2 && typeof item.testCaseCount === "number") {
                    countsMap[releaseId2] = item.testCaseCount;
                  }
                });
                setReleaseTestCaseCounts(countsMap);
              } else {
                setReleaseTestCaseCounts({});
              }
            }).catch(() => {
              console.warn("Failed to fetch test case counts");
              setReleaseTestCaseCounts({});
            }).finally(() => setReleaseLoading(false));
          } else {
            setReleaseTestCaseCounts({});
            setReleaseLoading(false);
          }
        } else {
          setReleaseError(releasesRes.message || "No releases found");
          setProjectReleaseCard([]);
          setReleaseTestCaseCounts({});
          setReleaseLoading(false);
        }
      }).catch((error) => {
        console.error("Error fetching releases:", error);
        setReleaseError("Failed to fetch releases. Please try again.");
        setReleaseTestCaseCounts({});
        setReleaseLoading(false);
      }).finally(() => setReleaseLoading(false));
    }
  }, [selectedProject]);
  useEffect(() => {
    getSeverities().then((res) => setSeverities(res.data.content)).catch((error) => {
      console.error("Failed to fetch severities:", error.message);
      setSeverities([]);
    });
  }, []);
  useEffect(() => {
    getDefectTypes().then((res) => {
      console.log("Fetched defect types:", res.data);
      setDefectTypes(res.data.content);
    }).catch((error) => {
      console.error("Failed to fetch defect types:", error.message);
      setDefectTypes([]);
    });
  }, []);
  useEffect(() => {
    getAllPriorities().then((res) => {
      console.log("Fetched priorities:", res.data.content);
      setPriorities(res.data.content);
    }).catch((error) => {
      console.error("Failed to fetch priorities:", error.message);
      setPriorities([]);
    });
  }, []);
  useEffect(() => {
    getAllDefectStatuses().then((res) => {
      console.log("Fetched defect statuses:", res.data);
      setDefectStatuses(res.data);
    }).catch((error) => {
      console.error("Failed to fetch defect statuses:", error.message);
      setDefectStatuses([]);
    });
  }, []);
  useEffect(() => {
    if (!selectedProject) {
      setAllocatedUsers([]);
      return;
    }
    setAllocatedUsersLoading(true);
    getUsersByModuleSubmoduleAllocation(parseInt(selectedProject)).then((response) => {
      console.log("response:", JSON.stringify(response, null, 2));
      let dataArray = [];
      if (Array.isArray(response)) {
        dataArray = response;
      } else if (Array.isArray(response?.data)) {
        dataArray = response.data;
      } else if (Array.isArray(response?.data?.data)) {
        dataArray = response.data.data;
      } else if (Array.isArray(response?.content)) {
        dataArray = response.content;
      }
      console.log("dataArray length:", dataArray.length);
      console.log("First item:", dataArray[0]);
      const filtered = dataArray.filter((emp) => {
        const roleType = (emp.roleType || "").toString().toUpperCase().trim();
        console.log(`${emp.firstName} → roleType: "${roleType}" → included: ${DEV_ROLE_TYPES.includes(roleType)}`);
        return DEV_ROLE_TYPES.includes(roleType);
      });
      console.log("filtered count:", filtered.length);
      const mapped = filtered.map((emp) => ({
        userId: emp.employeeId || emp.userId || emp.id,
        userName: emp.firstName && emp.lastName ? `${emp.firstName} ${emp.lastName}`.trim() : emp.userName || emp.name || "Unknown User",
        userRole: emp.roleName || emp.role || "",
        userWithRole: emp.employeeName || emp.userName || emp.name || ""
      }));
      setAllocatedUsers(mapped);
    }).catch((error) => {
      console.error("Failed to fetch developers:", error.message);
      setAllocatedUsers([]);
    }).finally(() => {
      setAllocatedUsersLoading(false);
    });
  }, [selectedProject]);
  useEffect(() => {
    const savedMapping = localStorage.getItem("testCaseDefectMapping");
    if (savedMapping) {
      try {
        const parsedMapping = JSON.parse(savedMapping);
        console.log(
          "Loaded test case defect mapping from localStorage:",
          parsedMapping
        );
        setLocalTestCaseDefectMap(parsedMapping);
        setTestCaseDefectMap(parsedMapping);
      } catch (error) {
        console.error(
          "Failed to parse test case defect mapping from localStorage:",
          error
        );
      }
    }
    const savedAssignments = localStorage.getItem("defectAssignments");
    if (savedAssignments) {
      try {
        const parsedAssignments = JSON.parse(savedAssignments);
        console.log(
          "Loaded defect assignments from localStorage:",
          parsedAssignments
        );
        setDefectAssignments(parsedAssignments);
      } catch (error) {
        console.error(
          "Failed to parse defect assignments from localStorage:",
          error
        );
      }
    }
  }, []);
  useEffect(() => {
    if (Object.keys(testCaseDefectMap).length > 0) {
      console.log("Global test case defect map updated:", testCaseDefectMap);
      setLocalTestCaseDefectMap(testCaseDefectMap);
      localStorage.setItem(
        "testCaseDefectMapping",
        JSON.stringify(testCaseDefectMap)
      );
    }
  }, [testCaseDefectMap]);
  useEffect(() => {
    return () => {
      setLocalTestCaseDefectMap({});
      setDefectAssignments({});
      setPendingStatusUpdate(null);
      console.log("Clearing pending status update - component unmounting");
    };
  }, []);
  useEffect(() => {
    const fetchExistingDefects = async () => {
      if (selectedProject) {
        setDefectsLoading(true);
        try {
          console.log(
            "Fetching existing defects for project:",
            selectedProject
          );
          const defects2 = await filterDefectsForTest({
            projectId: String(selectedProject),
            releaseId: selectedRelease ? parseInt(selectedRelease) : void 0
          });
          console.log("Fetched existing defects:", defects2);
          console.log("First defect full object:", defects2[0]);
          defects2.forEach((defect, index) => {
            console.log(`Defect ${index + 1}:`, {
              id: defect.id,
              defectId: defect.defectId,
              description: defect.description,
              moduleName: defect.moduleName,
              module_name: defect.module_name,
              subModuleName: defect.subModuleName,
              sub_module_name: defect.sub_module_name,
              assignedToName: defect.assignedToName,
              assigned_to_name: defect.assigned_to_name,
              testCaseId: defect.testCaseId,
              ReleaseTestCaseId: defect.ReleaseTestCaseId,
              releaseTestCaseId: defect.releaseTestCaseId
            });
          });
          setExistingDefects(Array.isArray(defects2) ? defects2 : []);
          console.log("wawa", defects2);
          c