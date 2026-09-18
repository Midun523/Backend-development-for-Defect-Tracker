import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface Modules {
  id: number;
  name: string;
  projectId?: number;
  description?: string;
  submodules?: any[];
  [key: string]: any;
}

export const getModulesByProject = async (projectId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.module(projectId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const items = res.data?.data || res.data || [];
  const arrayList: any = Array.isArray(items) ? [...items] : [];
  arrayList.data = arrayList;
  return arrayList;
};

export const getModulesByProjectId = getModulesByProject;

export const getAllModules = async (projectId?: number) => {
  if (projectId) {
    return getModulesByProject(projectId);
  }
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.getModules, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const items = res.data?.data || res.data || [];
  const arrayList: any = Array.isArray(items) ? [...items] : [];
  arrayList.data = arrayList;
  return arrayList;
};
