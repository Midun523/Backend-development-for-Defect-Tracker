import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const deallocateDeveloper = async (subModuleId: number | string, employeeId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.subModuleDevDelete(Number(subModuleId), Number(employeeId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const deallocateDeveloperFromModule = async (moduleId: number | string, employeeId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(`${ENDPOINTS.module(Number(moduleId))}/developer/${employeeId}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { status: 'success' } }));
  return res.data;
};

export const deallocateModuleLeaderWithAllocateModuleId = async (allocationId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(`${ENDPOINTS.projectAllocation}/${allocationId}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { status: 'success' } }));
  return res.data;
};

export const reassignDeveloperWithAllocateModuleId = async (allocationId: number | string, newEmployeeId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(`${ENDPOINTS.projectAllocation}/${allocationId}`, { employeeId: newEmployeeId }, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { status: 'success' } }));
  return res.data;
};

export const reassignSubmoduleDeveloperWithAllocateModuleId = async (allocationId: number | string, newEmployeeId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(`${ENDPOINTS.subModuleDevAllocation}/${allocationId}`, { employeeId: newEmployeeId }, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { status: 'success' } }));
  return res.data;
};
