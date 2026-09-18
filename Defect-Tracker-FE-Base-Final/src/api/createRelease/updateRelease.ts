import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const updateRelease = async (id: number, releaseData: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.releaseById(id), releaseData, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};
