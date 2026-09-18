import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const updateProject = async (id: number, projectData: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.projectById(id), projectData, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};
