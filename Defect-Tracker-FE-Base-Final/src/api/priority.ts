import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";

export interface Priority {
  id: number;
  name: string;
  color: string;
}

export interface GetPrioritiesResponse {
  status: string;
  message: string;
  data: {
    content: Priority[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  };
}

export const getAllPriorities = async (
  page?: number,
  pageSize?: number
): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const url = page !== undefined && pageSize !== undefined
    ? ENDPOINTS.priorityPagination(page, pageSize)
    : ENDPOINTS.priority;
  const res = await axios.get(url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });

  const apiResponse = res.data;
  const rawData = apiResponse?.data ?? apiResponse;
  let rawList: any[] = [];
  let totalPages = 1;
  let totalElements = 0;

  if (rawData && Array.isArray(rawData.content)) {
    rawList = rawData.content;
    totalPages = rawData.totalPages ?? 1;
    totalElements = rawData.totalElements ?? rawList.length;
  } else if (Array.isArray(rawData)) {
    rawList = rawData;
    totalElements = rawList.length;
    totalPages = pageSize ? Math.ceil(totalElements / pageSize) : 1;
  }

  const normalized = rawList.map((p: any) => {
    const rawColor = p.color || p.colorCode || "#3B82F6";
    const hex = rawColor.startsWith("#") ? rawColor : `#${rawColor}`;
    return {
      ...p,
      id: p.id,
      name: p.name || p.priorityName || "",
      priorityName: p.name || p.priorityName || "",
      color: hex,
      colorCode: hex,
    };
  });

  const dataPayload: any = {
    content: normalized,
    totalElements,
    totalPages,
    size: pageSize ?? normalized.length,
    number: page ?? 0,
    pageNumber: page ?? 0,
    pageSize: pageSize ?? normalized.length,
  };

  return {
    ...apiResponse,
    status: apiResponse?.status || "success",
    message: apiResponse?.message || "Priorities retrieved",
    statusMessage: apiResponse?.message || "Priorities retrieved",
    content: normalized,
    data: dataPayload,
  };
};

export const getPriorities = getAllPriorities;

export const createPriority = async (data: { name: string; color: string }) => {
  const token = localStorage.getItem("authToken");
  const payload = {
    ...data,
    color: data.color?.startsWith("#") ? data.color : `#${data.color || "3B82F6"}`,
    colorCode: data.color?.startsWith("#") ? data.color : `#${data.color || "3B82F6"}`,
  };
  const res = await axios.post(ENDPOINTS.priority, payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const updatePriority = async (id: number, data: { name: string; color: string }) => {
  const token = localStorage.getItem("authToken");
  const payload = {
    ...data,
    ...(data.color ? {
      color: data.color.startsWith("#") ? data.color : `#${data.color}`,
      colorCode: data.color.startsWith("#") ? data.color : `#${data.color}`,
    } : {}),
  };
  const res = await axios.put(ENDPOINTS.priorityById(id), payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const deletePriority = async (id: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.priorityById(id), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};