import axios from "axios";

export interface UpdateUserPayload {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  contactNo?: string;
  joinDate?: string;
  gender?: "Male" | "Female" | string;
  designationId?: number;
  experience?: number;
  availability?: number;
  skills?: string[] | string;
  isActive?: boolean | string;
}

export async function updateUser(id: number | string, userData: any) {
  const token = localStorage.getItem("authToken");
  const payload = {
    firstName: userData.firstName,
    lastName: userData.lastName,
    email: userData.email,
    phone: userData.contactNo || userData.phone,
    gender: userData.gender,
    designationId: userData.designationId ? Number(userData.designationId) : null,
    experience: Number(userData.experience) || 0,
    joinedDate: userData.joinDate || userData.joinedDate,
    skills: Array.isArray(userData.skills) ? userData.skills : (userData.skills ? [userData.skills] : []),
    availability: Number(userData.availability) || 100,
    status: typeof userData.isActive === "string" ? userData.isActive : (userData.isActive ? "active" : "inactive"),
    department: userData.department || "Engineering",
    manager: userData.manager || "Super Admin",
  };
  const res = await axios.put(`/api/v1/employee/${id}`, payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: "success",
    statusCode: res.data?.statusCode || 200,
    statusMessage: res.data?.message || "Employee updated successfully",
    message: "User updated successfully",
    data: res.data?.data,
  };
}

export async function updateUserStatus(id: number | string, status: boolean | string) {
  const statusStr = typeof status === "string" ? status : (status ? "active" : "inactive");
  const token = localStorage.getItem("authToken");
  const res = await axios.patch(`/api/v1/employee/${id}/status`, { status: statusStr }, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: "success",
    statusCode: res.data?.statusCode || 200,
    statusMessage: res.data?.message || "Employee status updated successfully",
    message: "User status updated successfully",
    data: res.data?.data,
  };
}
