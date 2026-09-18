import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface DefectDensityData {
  totalDefects?: number;
  kloc?: number;
  defectDensity?: number;
}

export const getKLOC = async (releaseId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.releaseKlocById(releaseId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: null } }));
  return res.data?.data;
};

export const getKILOC = async (projectId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.projectKloc(projectId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: { kloc: 0 } } }));
  return res.data;
};

export const getDefectDensity = async (projectId: number, releaseId?: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(`${ENDPOINTS.projectKloc(projectId)}/density`, {
    params: releaseId ? { releaseId } : {},
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: { totalDefects: 0, kloc: 0, defectDensity: 0 } } }));
  return res.data;
};

export const getProjectKLOC = async (projectId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.projectKloc(projectId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: null } }));
  return res.data?.data;
};
