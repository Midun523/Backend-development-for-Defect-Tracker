import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const updateModule = async (projectId: number, moduleId: number, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.moduleById(projectId, moduleId), data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};
