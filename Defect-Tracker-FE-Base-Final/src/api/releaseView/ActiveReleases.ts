import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getActiveReleases = async () => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.release, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};
