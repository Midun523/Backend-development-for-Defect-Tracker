import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const updateRole = async (id: number, data: { name: string; type?: string; roleName?: string }) => {
  const token = localStorage.getItem("authToken");
  const payload = {
    roleName: data.name || data.roleName,
    name: data.name || data.roleName,
    type: data.type || "",
  };
  const res = await axios.put(ENDPOINTS.roleById(id), payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    ...res.data,
    statusMessage: res.data?.message || 'Role updated successfully',
  };
};