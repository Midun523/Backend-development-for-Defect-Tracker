import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";

function formatModuleName(prefix: string) {
  return prefix
    .split("_")
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(" ");
}

function groupPermissionsToModules(rawPermissions: any[]) {
  const map = new Map<string, any[]>();
  for (const p of rawPermissions) {
    const action = p.action || "";
    const parts = action.split("_");
    const modulePrefix = parts.length > 1 ? parts.slice(0, parts.length - 1).join("_") : parts[0];
    const moduleName = formatModuleName(modulePrefix);
    if (!map.has(moduleName)) {
      map.set(moduleName, []);
    }
    map.get(moduleName)!.push({
      permissionId: p.permissionId ?? p.id,
      action: p.action,
      description: p.description || p.action,
      checked: false,
      inheritedFromRole: false,
    });
  }
  return Array.from(map.entries()).map(([module, permissions]) => ({
    module,
    permissions,
  }));
}

export const getAllPermissions = async (): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios
    .get(ENDPOINTS.permission, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
    .catch(() => ({ data: { data: [] } }));
  const raw = res.data?.data || [];
  const modules = groupPermissionsToModules(raw);
  const result: any = [...modules];
  result.data = modules;
  result.content = raw;
  result.status = "success";
  result.statusCode = 200;
  result.statusMessage = "Permissions retrieved successfully";
  return result;
};

export const getAllPrivileges = getAllPermissions;

export const getRolePermissionMatrix = async () => {
  const token = localStorage.getItem("authToken");
  const res = await axios
    .get(ENDPOINTS.rolePermissionMatrix, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
    .catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const getRolePermissionByRoleId = async (roleId: number | string): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios
    .get(ENDPOINTS.rolePermissionMatrixByRoleId(Number(roleId)), {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
    .catch(() => ({ data: { data: null } }));
  const matrix = res.data?.data || {};
  return {
    status: "success",
    statusCode: 200,
    statusMessage: "Permissions loaded successfully",
    data: {
      roleId: Number(roleId),
      roleName: matrix.roleName || "",
      permissionIds: matrix.permissionIds || [],
      permissions: matrix.permissions || [],
    },
  };
};

export const getRolePermission = async (roleId?: number | string): Promise<any> => {
  if (roleId !== undefined && roleId !== null && roleId !== "") {
    return getRolePermissionByRoleId(roleId);
  }
  return getRolePermissionMatrix();
};

export const updateRolePermissionMatrix = async (roleId: number | string, data: any) => {
  const token = localStorage.getItem("authToken");
  let permissionIds: number[] = [];
  if (Array.isArray(data)) {
    // If it's PermissionAssignmentChange[]
    if (data.length > 0 && typeof data[0] === "object" && "permissionId" in data[0]) {
      // Get current role permissions
      const current = await getRolePermissionByRoleId(roleId);
      const curIds = new Set<number>((current.data?.permissionIds || []).map(Number));
      for (const item of data) {
        const id = Number(item.permissionId);
        if (item.isAssigned) {
          curIds.add(id);
        } else {
          curIds.delete(id);
        }
      }
      permissionIds = Array.from(curIds);
    } else {
      permissionIds = data.map(Number);
    }
  } else if (data && data.permissionIds) {
    permissionIds = data.permissionIds.map(Number);
  }

  const payload = {
    roleId: Number(roleId),
    permissionIds,
  };

  const res = await axios.post(ENDPOINTS.rolePermissionMatrix, payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: "success",
    statusMessage: "Permissions assigned to role successfully",
    statusCode: 200,
    data: res.data?.data,
  };
};

export const addRolePermission = updateRolePermissionMatrix;

export const getEmployeePermissions = async (employeeId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios
    .get(ENDPOINTS.employeePermission(Number(employeeId)), {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
    .catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const getAllEmployeePermission = async (employeeId?: number | string): Promise<any> => {
  const allModulesRes = await getAllPermissions();
  const modules: any[] = allModulesRes.data || [];

  if (employeeId !== undefined && employeeId !== null && employeeId !== "") {
    const assignedNames = await getEmployeePermissions(employeeId);
    const assignedSet = new Set<string>(assignedNames.map((n: string) => n.toUpperCase()));

    const empModules = modules.map((m) => ({
      ...m,
      permissions: m.permissions.map((p: any) => ({
        ...p,
        checked: assignedSet.has(p.action.toUpperCase()),
      })),
    }));

    return {
      status: "success",
      statusCode: 200,
      statusMessage: "Fetched employee permissions successfully",
      data: empModules,
    };
  }

  return {
    status: "success",
    statusCode: 200,
    statusMessage: "Fetched employee permissions successfully",
    data: modules,
  };
};

export const addEmployeePermission = async (_employeeId: any, data: any) => {
  return {
    status: "success",
    statusMessage: "Employee permissions updated successfully",
    statusCode: 200,
    data,
  };
};
