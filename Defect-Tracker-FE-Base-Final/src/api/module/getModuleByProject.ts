import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getModuleByProject = async (projectId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.module(Number(projectId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};

export const getModulesByProject = getModuleByProject;
