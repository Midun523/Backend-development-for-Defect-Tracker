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

export const getAllReleaseTypes = async (page: number, size: number): Promise<ReleaseTypeResponse> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.releaseTypePagination(page, size), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const createReleaseType = async (data: CreateReleaseTypeRequest): Promise<Releasetype> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.releaseType, { name: data.releaseTypeName }, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || { id: 0, releaseTypeName: data.releaseTypeName };
};

export const updateReleaseType = async (id: number, data: UpdateReleaseTypeRequest): Promise<Releasetype> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.releaseTypeById(id), { name: data.releaseTypeName }, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || { id, releaseTypeName: data.releaseTypeName };
};

export const deleteReleaseType = async (id: number): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.releaseTypeById(id), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};