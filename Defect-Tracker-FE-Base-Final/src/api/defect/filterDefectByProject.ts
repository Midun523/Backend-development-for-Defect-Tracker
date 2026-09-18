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

export const filterDefectByProject = async (projectId: number, filters?: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.defectByProject(projectId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    params: filters,
  });
  return res.data?.data || [];
};

export const filterDefects = async (filters: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.defect, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    params: filters,
  });
  return res.data?.data || [];
};

export const getDefectsByProjectId = async (projectId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.defectByProject(Number(projectId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};

export const filterDefectsForTest = async (filters: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.defect, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    params: filters,
  }).catch(() => ({ data: { data: [] } }));
  return res.data;
};
