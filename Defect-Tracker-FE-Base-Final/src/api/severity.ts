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
  // The UI expects res.data.content to be the array of severities.
  // The backend ApiResponse has the paginated object in res.data.data
  // So res.data is the ApiResponse. We need to ensure res.data.content exists.
  const apiResponse = res.data;
  if (apiResponse && apiResponse.data) {
    const paginatedData = apiResponse.data;
    if (paginatedData.content) {
       // Attach content directly to the data object returned so res.data.content works
       apiResponse.content = paginatedData.content;
    } else if (Array.isArray(paginatedData)) {
       apiResponse.content = paginatedData;
    }
  } else if (Array.isArray(apiResponse)) {
     return { data: { content: apiResponse } } as any;
  }
  return apiResponse;
};

export const deleteSeverity = async (id: number) => {
  const res = await axios.delete(ENDPOINTS.severityById(id));
  return res.data;
};
