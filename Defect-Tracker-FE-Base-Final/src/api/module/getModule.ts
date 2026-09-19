import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";
import { getAllProjects } from "../projectget";

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
  try {
    const res = await axios.get("/api/v1/module", {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const items = res.data?.data || res.data || [];
    if (Array.isArray(items) && items.length > 0) {
      const arrayList: any = [...items];
      arrayList.data = arrayList;
      return arrayList;
    }
  } catch (e) {
    // Fallback: fetch projects and their modules
  }
  try {
    const projects = await getAllProjects();
    const allModules: any[] = [];
    for (const p of projects) {
      if (p.id) {
        const mods = await getModulesByProject(p.id);
        allModules.push(...mods);
      }
    }
    const arrayList: any = [...allModules];
    arrayList.data = arrayList;
    return arrayList;
  } catch (e) {
    const empty: any = [];
    empty.data = [];
    return empty;
  }
};
