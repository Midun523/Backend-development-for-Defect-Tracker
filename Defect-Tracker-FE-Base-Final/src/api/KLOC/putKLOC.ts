import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface CalculateKlocRequest {
  backendRepo: string;
  frontendRepo: string;
  githubUsername: string;
  githubToken: string;
}

export const putKLOC = async (releaseId: number, klocData: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.releaseKlocById(releaseId), klocData, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const updateProjectKloc = async (projectId: number, kloc: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.projectKloc(projectId), { kloc: Number(kloc) }, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: null } }));
  return res.data;
};

export const calculateKlocFromGithub = async (data: CalculateKlocRequest) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(`${ENDPOINTS.project}/calculate-kloc`, data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { totalKLOC: 10 } }));
  return res.data;
};
