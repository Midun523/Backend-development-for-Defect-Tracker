import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getDefectReopened = async (projectId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.dashboardReopenedByProject(projectId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const getReopenCountSummary = getDefectReopened;
