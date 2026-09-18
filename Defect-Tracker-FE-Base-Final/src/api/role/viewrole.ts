import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface Role {
  id: number;
  name: string;
  roleName?: string;
  type?: string;
  description?: string;
}

export interface GetRolesResponse {
  status: string;
  message: string;
  data: {
    content: Role[];
    totalElements: number;
    totalPages: number;
    pageNumber: number;
    pageSize: number;
  };
}

export const getAllRoles = async (page: number = 0, pageSize: number = 100): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.role(page, pageSize), {
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
    totalPages = Math.ceil(totalElements / pageSize) || 1;
    content = rawData.slice(page * pageSize, (page + 1) * pageSize);
  }

  const normalizedContent = content.map((r: any) => ({
    id: r.id,
    name: r.roleName || r.name || "",
    roleName: r.roleName || r.name || "",
    type: r.type || r.roleType || r.roleName || "",
    description: r.description,
  }));

  const contentArr: any = [...normalizedContent];
  contentArr.content = normalizedContent;
  contentArr.totalPages = totalPages;
  contentArr.totalElements = totalElements;
  contentArr.pageNumber = page;
  contentArr.pageSize = pageSize;

  return {
    ...res.data,
    statusMessage: res.data?.message || 'Success',
    data: contentArr,
  };
};
