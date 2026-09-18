import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";
import { Project } from "../types";

export const getAllProjects = async (): Promise<Project[]> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.project, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};

export const getAllProjectsForDashbord = async (): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.project, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: 'success',
    statusCode: 200,
    data: res.data?.data || [],
  };
};

export async function updateProject(id: number | string, projectData: any) {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.projectById(Number(id)), projectData, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: 'success',
    statusCode: 200,
    message: res.data?.message || 'Project updated successfully',
    data: res.data?.data,
  };
}

export async function deleteProject(id: string | number) {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.projectById(Number(id)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: 'success',
    statusCode: 200,
    message: res.data?.message || 'Project deleted successfully',
  };
}

export async function createProject(project: any) {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.project, project, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: 'success',
    statusCode: 200,
    message: res.data?.message || 'Project created successfully',
    data: res.data?.data,
  };
}

export interface AvailableManager {
  employeeId: number;
  firstName: string;
  lastName: string;
  email: string;
  designationId: number;
  designationName: string;
  availabilityPercent: number;
  isActive: boolean;
}

export const getAvailableManagers = async (designationId?: number): Promise<AvailableManager[]> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.availableManagers(designationId || 1), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const items = res.data?.data || [];
  return (Array.isArray(items) ? items : []).map((emp: any) => ({
    employeeId: emp.id,
    firstName: emp.firstName,
    lastName: emp.lastName,
    email: emp.email,
    designationId: emp.designation?.id || designationId || 1,
    designationName: emp.designation?.designationName || 'Manager',
    availabilityPercent: emp.availability ?? 100,
    isActive: emp.status === 'active' || emp.status === 'ACTIVE',
  }));
};

export const getAvailableManagersForUpdate = async (
  designationId?: number,
  projectId?: number
): Promise<AvailableManager[]> => {
  if (projectId && designationId) {
    const token = localStorage.getItem("authToken");
    const res = await axios.get(ENDPOINTS.availableManagersForUpdate(designationId, projectId), {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const items = res.data?.data || [];
    return (Array.isArray(items) ? items : []).map((emp: any) => ({
      employeeId: emp.id,
      firstName: emp.firstName,
      lastName: emp.lastName,
      email: emp.email,
      designationId: emp.designation?.id || designationId || 1,
      designationName: emp.designation?.designationName || 'Manager',
      availabilityPercent: emp.availability ?? 100,
      isActive: emp.status === 'active' || emp.status === 'ACTIVE',
    }));
  }
  return getAvailableManagers(designationId);
};
