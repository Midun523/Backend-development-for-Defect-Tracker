import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface Submodule {
  id: number;
  name: string;
  submoduleName?: string;
  subModuleName?: string;
  getSubModuleName?: string;
}

export interface GetSubmodulesResponse {
  status: string;
  message: string;
  data: Submodule[];
  statusCode: number;
}

export const getSubmodulesByModule = async (moduleId: number): Promise<GetSubmodulesResponse> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.subModule(moduleId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const items = res.data?.data || [];
  return {
    status: 'success',
    message: 'Submodules fetched successfully',
    statusCode: 200,
    data: (Array.isArray(items) ? items : []).map((s: any) => ({
      id: s.id,
      name: s.name || s.subModuleName || 'Submodule',
      submoduleName: s.name || s.subModuleName || 'Submodule',
      subModuleName: s.name || s.subModuleName || 'Submodule',
      getSubModuleName: s.name || s.subModuleName || 'Submodule',
    })),
  };
};

export const getSubmodulesByModuleId = async (moduleId: number): Promise<GetSubmodulesResponse> => {
  return getSubmodulesByModule(moduleId);
};
