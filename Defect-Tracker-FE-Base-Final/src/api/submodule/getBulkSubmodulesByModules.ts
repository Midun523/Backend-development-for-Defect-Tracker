import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface BulkSubmodule {
  subModuleId: number;
  subModuleName: string;
  moduleId: number;
  moduleName: string;
}

export const getBulkSubmodulesByModules = async (
  projectId: string | number,
  moduleIds: number[] | string
): Promise<BulkSubmodule[]> => {
  const token = localStorage.getItem("authToken");
  const ids = Array.isArray(moduleIds) ? moduleIds : String(moduleIds).split(',').map(Number);
  
  const res = await axios.post(ENDPOINTS.subModuleBulk(), { moduleIds: ids }, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));

  const items = res.data?.data || [];
  if (Array.isArray(items) && items.length > 0) {
    return items.map((s: any) => ({
      subModuleId: s.id || s.subModuleId,
      subModuleName: s.name || s.subModuleName || 'Submodule',
      moduleId: s.moduleId || s.module?.id,
      moduleName: s.moduleName || s.module?.name || 'Module',
    }));
  }

  // Fallback: fetch submodules per module
  const result: BulkSubmodule[] = [];
  for (const modId of ids) {
    try {
      const modRes = await axios.get(ENDPOINTS.subModule(modId), {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      const subs = modRes.data?.data || [];
      (Array.isArray(subs) ? subs : []).forEach((s: any) => {
        result.push({
          subModuleId: s.id,
          subModuleName: s.name || s.subModuleName || 'Submodule',
          moduleId: modId,
          moduleName: s.moduleName || 'Module',
        });
      });
    } catch { /* skip */ }
  }
  return result;
};
