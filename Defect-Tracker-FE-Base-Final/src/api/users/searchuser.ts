import axios from "axios";

export interface SearchUserData {
  id: number;
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
  userStatus: string;
  userGender: string;
  designationName: string;
}

export async function searchUsers(searchTerm: string) {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(`/api/v1/employee?page=0&size=1000&query=${encodeURIComponent(searchTerm)}`, {
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
    userStatus: emp.status === "active" || emp.status === "ACTIVE" ? "ACTIVE" : "INACTIVE",
    userGender: emp.gender,
    designationName: emp.designation?.designationName || "",
  }));
}
