import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";

export interface DefectType {
  id: number;
  name: string;
  defectTypeName?: string;
  description?: string;
}

export const getAllDefectTypes = async (page?: number, size?: number): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const params: any = {};
  if (page !== undefined) params.page = page;
  if (size !== undefined) params.size = size;

  const res = await axios.get(ENDPOINTS.defectType, {
    params,
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
    name: d.name || d.defectTypeName || "",
    defectTypeName: d.defectTypeName || d.name || "",
    description: d.description,
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

export const getDefectTypes = getAllDefectTypes;

export const createDefectType = async (data: any) => {
  const token = localStorage.getItem("authToken");
  const payload = {
    name: data.name || data.defectTypeName,
    defectTypeName: data.name || data.defectTypeName,
    description: data.description || "",
  };
  const res = await axios.post(ENDPOINTS.defectType, payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    ...res.data,
    statusMessage: res.data?.message || 'Defect type created successfully',
  };
};

export const updateDefectType = async (id: number | string, data: any) => {
  const token = localStorage.getItem("authToken");
  const payload = {
    name: data.name || data.defectTypeName,
    defectTypeName: data.name || data.defectTypeName,
    description: data.description || "",
  };
  const res = await axios.put(ENDPOINTS.defectTypeById(Number(id)), payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    ...res.data,
    statusMessage: res.data?.message || 'Defect type updated successfully',
  };
};

export const deleteDefectType = async (id: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.defectTypeById(Number(id)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    ...res.data,
    statusMessage: res.data?.message || 'Defect type deleted successfully',
  };
};
