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
  const res = await axios.post(ENDPOINTS.severity, data);
  return res.data;
};

export const updateSeverity = async (id: number, data: Partial<CreateSeverityRequest>): Promise<CreateSeverityResponse> => {
  const res = await axios.put(ENDPOINTS.severityById(id), data);
  return res.data;
};

export const getSeverities = async (
  page: number = 0,
  pageSize: number = 100
): Promise<GetSeveritiesResponse> => {
  const res = await axios.get(ENDPOINTS.severityPagination(page, pageSize));
  return res.data;
};

export const deleteSeverity = async (id: number) => {
  const res = await axios.delete(ENDPOINTS.severityById(id));
  return res.data;
};
