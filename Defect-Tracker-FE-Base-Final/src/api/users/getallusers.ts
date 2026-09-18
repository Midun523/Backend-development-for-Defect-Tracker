import axios from "axios";

export interface SimpleUser {
  id: number;
  userId: string;
  firstName: string;
  lastName: string;
  designationId?: number;
  designationName?: string;
  gender?: string;
  email?: string;
  contactNo?: string;
  joinDate?: string;
  isActive?: string;
  skills?: string[];
  experience?: number;
  availability?: number;
  currentProjects?: string[];
}

interface GetUsersByDesignationResponse {
  status: string;
  data: SimpleUser[];
}

function mapEmployee(emp: any) {
  return {
    id: emp.id,
    userId: emp.user?.userId || `US${emp.id}`,
    firstName: emp.firstName,
    lastName: emp.lastName,
    name: `${emp.firstName} ${emp.lastName}`.trim(),
    gender: emp.gender,
    email: emp.email,
    contactNo: emp.phone,
    designationId: emp.designation?.id,
    designationName: emp.designation?.designationName || "",
    joinDate: emp.joinedDate,
    isActive: emp.status === "active" || emp.status === "ACTIVE",
    skills: emp.skills ? (Array.isArray(emp.skills) ? emp.skills : emp.skills.split(",")) : [],
    experience: emp.experience,
    availability: emp.availability,
    currentProjects: [],
  };
}

export async function getAllUsers(page: number = 0, size: number = 10) {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(`/api/v1/employee?page=${page}&size=${size}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const pageData = res.data?.data;
  return {
    status: "success",
    statusCode: 200,
    data: {
      content: (pageData?.content || []).map(mapEmployee),
      totalElements: pageData?.totalElements || 0,
      totalPages: pageData?.totalPages || 1,
      size: pageData?.pageSize || size,
      number: pageData?.pageNumber || page,
    },
  };
}

export async function getAllUsersSimple() {
  const token = localStorage.getItem("authToken");
  const res = await axios.get("/api/v1/employee?size=1000", {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const items = res.data?.data;
  if (items && Array.isArray(items)) {
    return {
      status: "success",
      statusCode: 200,
      data: items.map(mapEmployee),
    };
  }
  // If paginated response
  const pageData = res.data?.data;
  return {
    status: "success",
    statusCode: 200,
    data: (pageData?.content || []).map(mapEmployee),
  };
}

export async function getUsersByDesignationId(designationId: number): Promise<GetUsersByDesignationResponse> {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(`/api/v1/designation/${designationId}/employee`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: "success",
    data: (res.data?.data || []).map((emp: any) => ({
      id: emp.id,
      userId: emp.user?.userId || `US${emp.id}`,
      firstName: emp.firstName,
      lastName: emp.lastName,
      designationId: emp.designation?.id,
      designationName: emp.designation?.designationName,
    })),
  };
}

export default getAllUsers;
