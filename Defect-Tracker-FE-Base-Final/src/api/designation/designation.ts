import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface Designations {
  id: number;
  name: string;
  designationName?: string;
  description?: string;
  totalEmployees?: number;
}

export const getAllDesignations = async (page?: number, size?: number) => {
  const token = localStorage.getItem("authToken");
  const url = page !== undefined && size !== undefined 
    ? ENDPOINTS.designationPagination(page, size) 
    : ENDPOINTS.designation;
  const res = await axios.get(url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });

  const rawData = res.data?.data;
  let content: any[] = [];
  let totalPages = 1;
  let totalElements = 0;

  if (rawData && Array.isArray(rawData.content)) {
    content = rawData.content;
    totalPages = rawData.totalPages ?? 1;
    totalElements = rawData.totalElements ?? content.length;
  } else if (Array.isArray(rawData)) {
    totalElements = rawData.length;
    if (page !== undefined && size !== undefined) {
      totalPages = Math.ceil(totalElements / size) || 1;
      content = rawData.slice(page * size, (page + 1) * size);
    } else {
      content = rawData;
    }
  }

  const normalizedContent = content.map((d: any) => ({
    id: d.id,
    name: d.designationName || d.name || "",
    designationName: d.designationName || d.name || "",
    description: d.description,
    totalEmployees: d.totalEmployees || 0,
  }));

  const contentArr: any = [...normalizedContent];
  contentArr.content = normalizedContent;
  contentArr.totalPages = totalPages;
  contentArr.totalElements = totalElements;
  contentArr.pageNumber = page || 0;
  contentArr.pageSize = size || normalizedContent.length;

  return {
    ...res.data,
    statusMessage: res.data?.message || 'Success',
    data: contentArr,
  };
};

export const getDesignations = getAllDesignations;

export const createDesignation = async (data: any) => {
  const token = localStorage.getItem("authToken");
  const payload = {
    designationName: data.name || data.designationName,
    name: data.name || data.designationName,
    description: data.description || "",
  };
  const res = await axios.post(ENDPOINTS.designation, payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    ...res.data,
    statusMessage: res.data?.message || 'Designation created successfully',
  };
};

export const updateDesignation = async (id: number | string, data: any) => {
  const token = localStorage.getItem("authToken");
  const payload = {
    designationName: data.name || data.designationName,
    name: data.name || data.designationName,
    description: data.description || "",
  };
  const res = await axios.put(ENDPOINTS.designationById(Number(id)), payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    ...res.data,
    statusMessage: res.data?.message || 'Designation updated successfully',
  };
};

export const putDesignation = updateDesignation;

export const deleteDesignation = async (id: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.designationById(Number(id)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    ...res.data,
    statusMessage: res.data?.message || 'Designation deleted successfully',
  };
};
