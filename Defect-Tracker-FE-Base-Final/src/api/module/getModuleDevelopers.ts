import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getModuleDevelopers = async (moduleIdOrSubmoduleId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.subModuleDev(Number(moduleIdOrSubmoduleId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const getDevelopersByModuleId = getModuleDevelopers;
