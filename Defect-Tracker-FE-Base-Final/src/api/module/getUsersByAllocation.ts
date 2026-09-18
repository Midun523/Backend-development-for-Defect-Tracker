import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface UserByAllocation {
  id?: number;
  userId?: number;
  employeeId?: number;
  name?: string;
  firstName?: string;
  lastName?: string;
  roleName?: string;
  [key: string]: any;
}

export const getUsersByAllocation = async (projectId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.projectAllocationProjectEmployee(Number(projectId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const getUsersBySubmoduleAllocation = async (
  projectIdOrSubmoduleId: number | string,
  _moduleId?: number | string,
  submoduleIdArg?: number | string,
) => {
  const submoduleId = submoduleIdArg !== undefined ? submoduleIdArg : projectIdOrSubmoduleId;
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.subModuleDev(Number(submoduleId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  const data = res.data?.data || [];
  return (Array.isArray(data) ? data : []).map((d: any) => ({
    id: d.id,
    userId: d.employeeId || d.employee?.id,
    employeeId: d.employeeId || d.employee?.id,
    name: d.employeeName || (d.employee ? `${d.employee.firstName} ${d.employee.lastName}` : "Developer"),
    firstName: d.employee?.firstName || d.employeeName?.split(" ")[0] || "Developer",
    lastName: d.employee?.lastName || d.employeeName?.split(" ")[1] || "",
    roleName: d.employee?.designation?.designationName || "Developer",
    userWithRole: `${d.employeeName || (d.employee ? `${d.employee.firstName} ${d.employee.lastName}` : "Developer")}-${d.employee?.designation?.designationName || "Developer"}`,
  }));
};

export const getUsersByModuleSubmoduleAllocation = async (moduleId?: number | string, submoduleId?: number | string) => {
  const token = localStorage.getItem("authToken");
  if (submoduleId) {
    const res = await axios.get(ENDPOINTS.subModuleDev(Number(submoduleId)), {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    }).catch(() => ({ data: { data: [] } }));
    const data = res.data?.data || [];
    return (Array.isArray(data) ? data : []).map((d: any) => ({
      id: d.id,
      userId: d.employeeId || d.employee?.id,
      employeeId: d.employeeId || d.employee?.id,
      name: d.employeeName || (d.employee ? `${d.employee.firstName} ${d.employee.lastName}` : "Developer"),
      firstName: d.employee?.firstName || d.employeeName?.split(" ")[0] || "Developer",
      lastName: d.employee?.lastName || d.employeeName?.split(" ")[1] || "",
      roleName: d.employee?.designation?.designationName || "Developer",
      userWithRole: `${d.employeeName || (d.employee ? `${d.employee.firstName} ${d.employee.lastName}` : "Developer")}-${d.employee?.designation?.designationName || "Developer"}`,
    }));
  }
  if (moduleId) {
    const res = await axios.get(ENDPOINTS.projectAllocationProjectEmployee(Number(moduleId)), {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    }).catch(() => ({ data: { data: [] } }));
    return res.data?.data || [];
  }
  return [];
};
