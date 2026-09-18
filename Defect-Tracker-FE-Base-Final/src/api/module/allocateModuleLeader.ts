import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const allocateModuleLeader = async (data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.ALLOCATE_MODULE_LEADER, data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const deallocateModuleLeader = async (allocateModuleId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.DEALLOCATE_MODULE_LEADER(allocateModuleId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const getModuleAllocatedLeader = async (moduleId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.GET_MODULE_ALLOCATED_LEADER(moduleId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: null } }));
  return res.data?.data;
};
