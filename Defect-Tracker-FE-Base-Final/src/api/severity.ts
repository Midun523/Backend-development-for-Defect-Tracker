import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";

export interface Severity {
  id: number;
  name: string;
  color: string;
  weight: number;
}

export interface CreateSeverityRequest {
  name: string;
  color: string;
  weight: number;
}

export interface CreateSeverityResponse {
  status: string;
  message: string;
  statusCode: number;
  data?: Severity;
}

export interface GetSeveritiesResponse {
  status: string;
  message: string;
  data: {
    content: Severity[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  };
}

export const createSeverity = async (data: CreateSeverityRequest): Promise<CreateSeverityResponse> => {
  const token = localStorage.getItem("authToken");
  const payload = {
    ...data,
    color: data.color?.startsWith("#") ? data.color : `#${data.color || "EF4444"}`,
    colorCode: data.color?.startsWith("#") ? data.color : `#${data.color || "EF4444"}`,
  };
  const res = await axios.post(ENDPOINTS.severity, payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const updateSeverity = async (id: number, data: Partial<CreateSeverityRequest>): Promise<CreateSeverityResponse> => {
  const token = localStorage.getItem("authToken");
  const payload = {
    ...data,
    ...(data.color ? {
      color: data.color.startsWith("#") ? data.color : `#${data.color}`,
      colorCode: data.color.startsWith("#") ? data.color : `#${data.color}`,
    } : {}),
  };
  const res = await axios.put(ENDPOINTS.severityById(id), payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const getSeverities = async (
  page: number = 0,
  pageSize: number = 100
): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.severityPagination(page, pageSize), {
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

  const normalized = rawList.map((s: any) => {
    const rawColor = s.color || s.colorCode || "#EF4444";
    const hex = rawColor.startsWith("#") ? rawColor : `#${rawColor}`;
    return {
      ...s,
      id: s.id,
      name: s.name || s.severityName || "",
      severityName: s.name || s.severityName || "",
      color: hex,
      colorCode: hex,
      weight: s.weight ?? 1,
    };
  });

  const dataPayload: any = {
    content: normalized,
    totalElements,
    totalPages,
    size: pageSize,
    number: page,
    pageNumber: page,
    pageSize,
  };

  return {
    ...apiResponse,
    status: apiResponse?.status || "success",
    message: apiResponse?.message || "Severities retrieved",
    statusMessage: apiResponse?.message || "Severities retrieved",
    content: normalized,
    data: dataPayload,
  };
};

export const getAllSeverities = getSeverities;

export const deleteSeverity = async (id: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.severityById(id), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};
