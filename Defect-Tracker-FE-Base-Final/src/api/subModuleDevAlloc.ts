import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";

export interface SubModuleDevResponse {
  status: string;
  statusCode: number | string;
  statusMessage: string;
  data: SubModuleDevAllocation[];
}

export interface SubModuleDevAllocation {
  id: number | string;
  employeeId: number | string;
  submoduleId: number | string;
  employeeName?: string;
}

export const getAllSubmoduleAllocatedDevBySubmoduleId = async (
  subModuleId: number
): Promise<SubModuleDevResponse> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.subModuleDev(subModuleId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: 'success',
    statusCode: 200,
    statusMessage: 'Success',
    data: res.data?.data || [],
  };
};

export const allocateProjectEmployeeToSubModule = async (
  subModuleId: number,
  employeeId: number
): Promise<SubModuleDevResponse> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.subModuleDev(subModuleId), { employeeId }, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: 'success',
    statusCode: 200,
    statusMessage: res.data?.message || 'Developer allocated to submodule successfully',
    data: res.data?.data ? [res.data.data] : [],
  };
};

export const deAllocateProjectEmployeeFromSubModule = async (
  subModuleId: number,
  employeeId: number
): Promise<SubModuleDevResponse> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.subModuleDevDelete(subModuleId, employeeId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: 'success',
    statusCode: 200,
    statusMessage: res.data?.message || 'Developer deallocated from submodule successfully',
    data: [],
  };
};
