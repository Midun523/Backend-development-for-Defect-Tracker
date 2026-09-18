import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getSubmodulesByModule = async (moduleId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.subModule(moduleId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const items = res.data?.data || [];
  return {
    status: 'success',
    statusCode: 200,
    data: (Array.isArray(items) ? items : []).map((s: any) => ({
      ...s,
      subModuleName: s.name || s.subModuleName,
    })),
  };
};

export const getSubmodulesByModuleId = getSubmodulesByModule;
