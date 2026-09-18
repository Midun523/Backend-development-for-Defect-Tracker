import axios from "axios";

export interface UserFilter {
  id: number;
  userId?: string;
  firstName: string;
  lastName: string;
  email: string;
  userGender?: string;
  status?: string;
  designationId?: number;
  designationName?: string;
}

export async function getUsersByFilter(
  gender?: string,
  status?: string,
  designationId?: number,
  page: number = 1,
  size: number = 10
) {
  const token = localStorage.getItem("authToken");
  // Build query params for backend filtering
  let url = `/api/v1/employee?page=${page - 1}&size=${size}`;
  if (gender) url += `&gender=${encodeURIComponent(gender)}`;
  if (status) url += `&status=${encodeURIComponent(status)}`;
  if (designationId) url += `&designationId=${designationId}`;

  const res = await axios.get(url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const pageData = res.data?.data;
  const items = pageData?.content || pageData || [];
  return (Array.isArray(items) ? items : []).map((emp: any) => ({
    id: emp.id,
    userId: emp.user?.userId || `US${emp.id}`,
    firstName: emp.firstName,
    lastName: emp.lastName,
    email: emp.email,
    userGender: emp.gender,
    status: emp.status,
    designationId: emp.designation?.id,
    designationName: emp.designation?.designationName || "",
  }));
}

export const filterUsers = getUsersByFilter;
