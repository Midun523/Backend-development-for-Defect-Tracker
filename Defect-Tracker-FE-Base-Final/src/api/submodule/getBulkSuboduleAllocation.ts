import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getBulkSuboduleAllocation = async (
  projectId: number,
  moduleId: number,
  submoduleId: number
) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.subModuleDev(submoduleId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const items = res.data?.data || [];
  return (Array.isArray(items) ? items : []).map((a: any) => ({
    id: a.id,
    employeeId: a.employeeId || a.employee?.id,
    employeeName: a.employeeName || (a.employee ? `${a.employee.firstName} ${a.employee.lastName}` : 'Developer'),
    projectId,
    moduleId,
    submoduleId,
    role: a.role || 'Developer',
  }));
};