import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getDefectsByModule = async (projectId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.defectByModules(projectId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};
