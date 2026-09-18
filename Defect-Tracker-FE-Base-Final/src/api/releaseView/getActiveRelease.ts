import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getActiveRelease = async (releaseId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.releaseById(releaseId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data;
};
