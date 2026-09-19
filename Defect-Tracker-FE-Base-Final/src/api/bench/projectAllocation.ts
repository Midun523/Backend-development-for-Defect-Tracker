import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getAllProjectAllocations = async () => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.projectAllocation, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};

export const allocateToProject = async (data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.projectAllocation, data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const postProjectAllocations = allocateToProject;

export const getProjectAllocationsById = async (id: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(`${ENDPOINTS.projectAllocation}/${id}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: null } }));
  return res.data;
};

export const updateProjectAllocation = async (id: number | string, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(`${ENDPOINTS.projectAllocation}/${id}`, data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const deleteProjectAllocation = async (id: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(`${ENDPOINTS.projectAllocation}/${id}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const getMaxAvailablePercentage = async (_employeeId: any) => {
  return { data: 100 };
};

export const deallocateFromProject = async (employeeId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.projectAllocationDeallocate(employeeId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const extendAllocation = async (employeeId: number, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.projectAllocationExtend(employeeId), data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const getProjectAllocatedEmployees = async (projectId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.projectAllocationProjectEmployee(projectId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};

export const getEmployeeAllocations = async (employeeId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.projectAllocationByEmployee(employeeId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};

export const getViewAllocations = async (employeeId?: any) => {
  return { status: 'success', data: [] };
};

export const getDevelopersWithRolesByProjectId = async (projectId: number | string) => {
  const token = localStorage.getItem("authToken");
  try {
    const res = await axios.get(ENDPOINTS.projectAllocationProjectEmployee(Number(projectId)), {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const allocations = res.data?.data || [];
    if (Array.isArray(allocations) && allocations.length > 0) {
      return {
        data: allocations.map((alloc: any) => {
          const empId = alloc.employeeId || alloc.employee?.id || alloc.employee?.userId || alloc.id;
          const fullName =
            alloc.employeeName ||
            (alloc.employee?.firstName ? `${alloc.employee.firstName} ${alloc.employee.lastName || ""}`.trim() : alloc.employee?.name) ||
            "Developer";
          const roleName = alloc.role?.name || alloc.roleName || "Developer";
          return {
            id: empId,
            userId: empId,
            employeeId: empId,
            name: fullName,
            userName: fullName,
            userFullName: fullName,
            userWithRole: `${fullName} - ${roleName}`,
            role: roleName,
            roleName: roleName,
            roleId: alloc.role?.id || alloc.roleId || 1,
            projectAllocationId: alloc.id,
          };
        }),
      };
    }
  } catch (e) {
    console.warn("Failed to fetch project allocated developers, falling back to all employees:", e);
  }

  try {
    const res = await axios.get(ENDPOINTS.employee, {
      params: { size: 1000 },
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const users = res.data?.data?.content || res.data?.data || [];
    return {
      data: users.map((emp: any) => {
        const fullName = `${emp.firstName || ""} ${emp.lastName || ""}`.trim() || emp.name || "User";
        const roleName = emp.roleName || emp.role?.name || "Developer";
        return {
          id: emp.id,
          userId: emp.id,
          employeeId: emp.id,
          name: fullName,
          userName: fullName,
          userFullName: fullName,
          userWithRole: `${fullName} - ${roleName}`,
          role: roleName,
          roleName: roleName,
          roleId: emp.roleId || 1,
          projectAllocationId: emp.id,
        };
      }),
    };
  } catch {
    return { data: [] };
  }
};
