import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface ProjectRelease {
  id: number;
  releaseId?: number;
  releaseName?: string;
  name?: string;
  projectId?: number;
  projectName?: string;
  status?: string;
  releaseDate?: string;
  description?: string;
  [key: string]: any;
}

export const getProjectReleaseCardView = async (projectId: number | string) => {
  const token = localStorage.getItem("authToken");
  try {
    const res = await axios.get(`${ENDPOINTS.release}?projectId=${projectId}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const list = res.data?.data || res.data || [];
    const arrayList = Array.isArray(list) ? list : (list ? [list] : []);
    return { 
      status: "Success",
      statusCode: "200",
      data: arrayList 
    };
  } catch {
    try {
      const res = await axios.get(ENDPOINTS.releaseActiveByProject(Number(projectId)), {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      const active = res.data?.data || res.data;
      return { 
        status: "Success",
        statusCode: "200",
        data: active ? [active] : [] 
      };
    } catch {
      return { data: [] };
    }
  }
};

export const projectReleaseCardView = getProjectReleaseCardView;

export const getReleaseTestCaseCountsLoad = async (projectId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.releaseActiveByProject(Number(projectId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  return res.data;
};
