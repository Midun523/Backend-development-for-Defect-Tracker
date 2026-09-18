import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const deleteModule = async (projectId: number, moduleId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.moduleById(projectId, moduleId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};
