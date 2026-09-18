import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const updateReleaseStatus = async (releaseId: number, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.patch(ENDPOINTS.releaseStatus(releaseId), data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};
