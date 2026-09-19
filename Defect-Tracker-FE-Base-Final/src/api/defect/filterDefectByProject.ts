import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface FilteredDefect {
  id: number;
  defectId?: string;
  title?: string;
  description: string;
  steps?: string;
  moduleId?: number;
  moduleName?: string;
  subModuleId?: number;
  subModuleName?: string;
  severityId?: number;
  severityName?: string;
  priorityId?: number;
  priorityName?: string;
  typeId?: number;
  defectTypeName?: string;
  statusId?: number;
  statusName?: string;
  status?: string;
  assignedToId?: number;
  assignedToName?: string;
  assignedById?: number;
  assignedByName?: string;
  releaseId?: number;
  releaseName?: string;
  createdAt?: string;
  updatedAt?: string;
  [key: string]: any;
}

export const normalizeDefect = (d: any): FilteredDefect => {
  if (!d) return d;
  const projectName = d.projectName || d.project_name || d.project?.name || "";
  const moduleName = d.moduleName || d.module_name || d.module?.name || "";
  const subModuleName = d.subModuleName || d.sub_module_name || d.subModule?.name || "";
  const severityName = d.severityName || d.severity_name || d.severity?.name || "";
  const priorityName = d.priorityName || d.priority_name || d.priority?.name || "";
  const defectTypeName = d.defectTypeName || d.defect_type_name || d.defectType?.name || d.typeName || "";
  const rawStatusName = d.statusName || d.defect_status_name || d.status || d.defectStatus?.name || "OPEN";
  const assignedToName =
    d.assignedToName ||
    d.assigned_to_name ||
    (d.assignedTo?.firstName ? `${d.assignedTo.firstName} ${d.assignedTo.lastName || ""}`.trim() : d.assignedTo?.name || "");
  const assignedByName =
    d.assignedByName ||
    d.assigned_by_name ||
    (d.assignedBy?.firstName ? `${d.assignedBy.firstName} ${d.assignedBy.lastName || ""}`.trim() : d.assignedBy?.name || "");
  const releaseName = d.releaseName || d.release_name || d.release?.releaseName || d.release?.releaseVersion || "";

  const sevColor = d.severity?.color || d.severityColor || "#EF4444";
  const priColor = d.priority?.color || d.priorityColor || "#3B82F6";
  const statColor = d.defectStatus?.color || d.statusColor || "#6B7280";

  return {
    ...d,
    id: d.id,
    defectId: d.defectId || `DEF${d.id}`,
    title: d.title || "",
    description: d.description || "",
    steps: d.steps || "",
    // Project aliases
    projectId: d.projectId || d.project?.id,
    project_id: d.projectId || d.project?.id,
    projectName,
    project_name: projectName,
    // Module aliases
    moduleId: d.moduleId || d.module?.id,
    module_id: d.moduleId || d.module?.id,
    moduleName,
    module_name: moduleName,
    // Submodule aliases
    subModuleId: d.subModuleId || d.subModule?.id,
    sub_module_id: d.subModuleId || d.subModule?.id,
    subModuleName,
    sub_module_name: subModuleName,
    // Severity aliases
    severityId: d.severityId || d.severity?.id,
    severity_id: d.severityId || d.severity?.id,
    severityName,
    severity_name: severityName,
    severityColor: sevColor,
    severityColorCode: sevColor.startsWith("#") ? sevColor : `#${sevColor}`,
    // Priority aliases
    priorityId: d.priorityId || d.priority?.id,
    priority_id: d.priorityId || d.priority?.id,
    priorityName,
    priority_name: priorityName,
    priorityColor: priColor,
    priorityColorCode: priColor.startsWith("#") ? priColor : `#${priColor}`,
    // Defect Type aliases
    defectTypeId: d.defectTypeId || d.typeId || d.defectType?.id,
    defect_type_id: d.defectTypeId || d.typeId || d.defectType?.id,
    defectTypeName,
    defect_type_name: defectTypeName,
    typeId: d.defectTypeId || d.typeId || d.defectType?.id,
    typeName: defectTypeName,
    // Status aliases (supporting UPPER_SNAKE_CASE, lowercase-hyphenated, and raw)
    statusId: d.statusId || d.defectStatusId || d.defectStatus?.id,
    defect_status_id: d.statusId || d.defectStatusId || d.defectStatus?.id,
    statusName: rawStatusName,
    defect_status_name: rawStatusName,
    status: rawStatusName,
    statusUpper: rawStatusName.toUpperCase().replace(/\s+/g, "_"),
    statusLower: rawStatusName.toLowerCase().replace(/[\s_]+/g, "-"),
    statusColor: statColor,
    statusColorCode: statColor.startsWith("#") ? statColor : `#${statColor}`,
    // Assigned to aliases
    assignedToId: d.assignedToId || d.assignedTo?.id,
    assigned_to_id: d.assignedToId || d.assignedTo?.id,
    assignedToName,
    assigned_to_name: assignedToName,
    // Assigned by aliases
    assignedById: d.assignedById || d.assignedBy?.id,
    assigned_by_id: d.assignedById || d.assignedBy?.id,
    assignedByName,
    assigned_by_name: assignedByName,
    // Release aliases
    releaseId: d.releaseId || d.release?.id,
    release_id: d.releaseId || d.release?.id,
    releaseName,
    release_name: releaseName,
    // Reopen aliases
    reOpenCount: d.reOpenCount ?? d.reopenCount ?? 0,
    reopenCount: d.reOpenCount ?? d.reopenCount ?? 0,
    // TestCase Required
    testCaseRequired: d.testCaseRequired ?? false,
    testcase_required: d.testCaseRequired ?? false,
    attachment: d.attachment || "",
  };
};

export const filterDefectByProject = async (projectId: number, filters?: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.defectByProject(projectId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    params: filters,
  });
  const list = res.data?.data?.content || res.data?.data || res.data || [];
  return (Array.isArray(list) ? list : []).map(normalizeDefect);
};

export const filterDefects = async (filters: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.defect, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    params: filters,
  });
  const list = res.data?.data?.content || res.data?.data || res.data || [];
  return (Array.isArray(list) ? list : []).map(normalizeDefect);
};

export const getDefectsByProjectId = async (projectId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.defectByProject(Number(projectId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const list = res.data?.data?.content || res.data?.data || res.data || [];
  return (Array.isArray(list) ? list : []).map(normalizeDefect);
};

export const filterDefectsForTest = async (filters: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios
    .get(ENDPOINTS.defect, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      params: filters,
    })
    .catch(() => ({ data: { data: [] } }));
  const rawData = res.data?.data;
  const list = rawData?.content || rawData || [];
  const normalized = (Array.isArray(list) ? list : []).map(normalizeDefect);
  return {
    ...res.data,
    data: normalized,
  };
};
