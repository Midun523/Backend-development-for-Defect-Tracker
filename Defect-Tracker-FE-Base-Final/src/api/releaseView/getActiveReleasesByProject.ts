import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getActiveReleasesByProject = async (projectId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.releaseActiveByProject(projectId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};
