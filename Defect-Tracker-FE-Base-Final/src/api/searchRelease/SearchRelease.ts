import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const searchRelease = async (query: string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(`${ENDPOINTS.release}?query=${encodeURIComponent(query)}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};
