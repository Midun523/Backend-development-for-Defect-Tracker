import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";

export interface DefectStatus {
  id: number;
  name: string;
  color: string;
  type?: string;
  orderIndex?: number;
  statusType?: string;
  defectStatusName?: string;
  statusName?: string;
  colorCode?: string;
  description?: string;
  default?: boolean;
}

export interface CreateDefectStatusRequest {
  name: string;
  color: string;
  type?: string;
}

const normalizeStatus = (s: any): DefectStatus => {
  if (!s) return s;
  const name = s.name || s.defectStatusName || s.statusName || "";
  const color = s.color || s.colorCode || "#6B7280";
  const type = s.type || s.statusType || "";
  return {
    ...s,
    id: s.id,
    name,
    statusName: name,
    defectStatusName: name,
    color,
    colorCode: color,
    type,
    statusType: type,
    description: s.description || "",
    orderIndex: s.orderIndex !== undefined ? s.orderIndex : 0,
    default: s.default ?? false,
  };
};

export const getAllDefectStatuses = async (page?: number, size?: number) => {
  const token = localStorage.getItem("authToken");
  const params: any = {};
  if (page !== undefined) params.page = page;
  if (size !== undefined) params.size = size;

  const res = await axios.get(ENDPOINTS.statusType, {
    params,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });

  const rawData = res.data?.data ?? res.data;
  let rawList: any[] = [];
  let totalPages = 1;
  let totalElements = 0;
  let pageNumber = page ?? 0;
  let pageSizeVal = size ?? 10;

  if (rawData && Array.isArray(rawData.content)) {
    rawList = rawData.content;
    totalPages = rawData.totalPages ?? 1;
    totalElements = rawData.totalElements ?? rawList.length;
    pageNumber = rawData.pageNumber ?? 0;
    pageSizeVal = rawData.pageSize ?? rawList.length;
  } else if (Array.isArray(rawData)) {
    rawList = rawData;
    totalPages = 1;
    totalElements = rawList.length;
    pageNumber = 0;
    pageSizeVal = rawList.length;
  }

  const normalizedList = rawList.map(normalizeStatus);

  // Return a composite object that works as an array AND has pagination fields + .data + .content
  const result: any = [...normalizedList];
  result.content = normalizedList;
  result.data = normalizedList;
  result.totalPages = totalPages;
  result.totalElements = totalElements;
  result.pageNumber = pageNumber;
  result.pageSize = pageSizeVal;
  return result;
};

export const getDefectStatus = async (defectId: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.defectById(defectId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data;
};

export const createDefectStatus = async (data: CreateDefectStatusRequest) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.statusType, data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const body = res.data?.data || res.data || {};
  return {
    ...normalizeStatus(body),
    statusMessage: res.data?.statusMessage || res.data?.message || "Created successfully",
    statusCode: res.status,
  };
};

export const updateDefectStatus = async (id: number, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(`${ENDPOINTS.statusType}/${id}`, data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const body = res.data?.data || res.data || {};
  return {
    ...normalizeStatus(body),
    statusMessage: res.data?.statusMessage || res.data?.message || "Updated successfully",
    statusCode: res.status,
  };
};

export const deleteDefectStatus = async (id: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(`${ENDPOINTS.statusType}/${id}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: "success",
    statusMessage: res.data?.statusMessage || res.data?.message || "Deleted successfully",
    statusCode: res.status,
    data: null,
  };
};
