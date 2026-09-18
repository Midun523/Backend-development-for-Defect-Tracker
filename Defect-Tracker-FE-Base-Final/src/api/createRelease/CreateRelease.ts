import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const createRelease = async (releaseData: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.release, releaseData, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};
