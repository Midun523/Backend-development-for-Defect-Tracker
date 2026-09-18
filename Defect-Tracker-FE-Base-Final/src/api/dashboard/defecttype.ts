import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getDefectsByType = async (projectId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.defectByType(projectId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const getDefectTypeByProjectId = getDefectsByType;
