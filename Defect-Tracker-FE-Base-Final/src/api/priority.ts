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
  page: number = 0,
  pageSize: number = 100
): Promise<GetPrioritiesResponse> => {
  const res = await axios.get(ENDPOINTS.priorityPagination(page, pageSize));
  return res.data;
};

export const createPriority = async (data: { name: string; color: string }) => {
  const res = await axios.post(ENDPOINTS.priority, data);
  return res.data;
};

export const updatePriority = async (id: number, data: { name: string; color: string }) => {
  const res = await axios.put(ENDPOINTS.priorityById(id), data);
  return res.data;
};

export const deletePriority = async (id: number) => {
  const res = await axios.delete(ENDPOINTS.priorityById(id));
  return res.data;
};