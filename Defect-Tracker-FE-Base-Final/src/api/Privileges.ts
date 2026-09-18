import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";

export const getRolePermissionMatrix = async () => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.rolePermissionMatrix, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const getRolePermission = getRolePermissionMatrix;

export const getRolePermissionByRoleId = async (roleId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.rolePermissionMatrixByRoleId(Number(roleId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const updateRolePermissionMatrix = async (roleId: number | string, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.rolePermissionMatrixByRoleId(Number(roleId)), data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const addRolePermission = updateRolePermissionMatrix;

export const getEmployeePermissions = async (employeeId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.employeePermission(Number(employeeId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const getAllEmployeePermission = async () => {
  return [];
};

export const addEmployeePermission = async (_employeeId: any, data: any) => {
  return { status: 'success', data };
};

export const getAllPermissions = async () => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.permission, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const getAllPrivileges = getAllPermissions;
