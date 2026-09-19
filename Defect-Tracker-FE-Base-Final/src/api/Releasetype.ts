import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";

export interface Releasetype {
  id: number;
  releaseTypeName: string;
}

export interface ReleaseTypeList {
  totalElements?: number;
  content?: Releasetype[];
}

export interface ReleaseTypeResponse {
  status: string;
  statusMessage: string;
  data: ReleaseTypeList;
  statusCode: number;
}

export interface CreateReleaseTypeRequest {
  releaseTypeName: string;
}

export interface UpdateReleaseTypeRequest {
  releaseTypeName: string;
}

export const getAllReleaseTypes = async (page: number = 0, size: number = 100): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.releaseTypePagination(page, size), {
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
    totalPages = size ? Math.ceil(totalElements / size) : 1;
  }

  const normalized = rawList.map((type: any) => ({
    ...type,
    id: type.id,
    name: type.name || type.releaseTypeName || "",
    releaseTypeName: type.releaseTypeName || type.name || "",
    description: type.description || "",
  }));

  const dataPayload: any = {
    content: normalized,
    totalElements,
    totalPages,
    size,
    number: page,
    pageNumber: page,
    pageSize: size,
  };

  return {
    ...apiResponse,
    status: apiResponse?.status || "success",
    message: apiResponse?.message || "Release types retrieved",
    statusMessage: apiResponse?.message || "Release types retrieved",
    content: normalized,
    data: dataPayload,
  };
};

export const createReleaseType = async (data: CreateReleaseTypeRequest): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const name = data.releaseTypeName;
  const res = await axios.post(ENDPOINTS.releaseType, { name, releaseTypeName: name }, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const saved = res.data?.data || res.data || {};
  return {
    id: saved.id || 0,
    name: saved.name || name,
    releaseTypeName: saved.releaseTypeName || saved.name || name,
    ...saved,
  };
};

export const updateReleaseType = async (id: number, data: UpdateReleaseTypeRequest): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const name = data.releaseTypeName;
  const res = await axios.put(ENDPOINTS.releaseTypeById(id), { name, releaseTypeName: name }, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const saved = res.data?.data || res.data || {};
  return {
    id,
    name: saved.name || name,
    releaseTypeName: saved.releaseTypeName || saved.name || name,
    ...saved,
  };
};

export const deleteReleaseType = async (id: number): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.releaseTypeById(id), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};