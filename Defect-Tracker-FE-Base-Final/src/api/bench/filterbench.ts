import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const filterBench = async (filters?: any) => {
  const token = localStorage.getItem("authToken");
  let url = ENDPOINTS.benchEmployee;
  const params = new URLSearchParams();
  if (filters?.designationId) params.append('designationId', String(filters.designationId));
  if (filters?.skills) params.append('skills', filters.skills);
  const qs = params.toString();
  if (qs) url += '?' + qs;
  const res = await axios.get(url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};
