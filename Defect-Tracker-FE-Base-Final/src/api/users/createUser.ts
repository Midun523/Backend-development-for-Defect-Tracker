import axios from "axios";

export async function createUser(userData: any) {
  const payload = {
    firstName: userData.firstName,
    lastName: userData.lastName,
    email: userData.email,
    phone: userData.contactNo || userData.phone || "+1 555-0100",
    gender: userData.gender || "Male",
    designationId: userData.designationId ? Number(userData.designationId) : null,
    experience: Number(userData.experience) || 0,
    joinedDate: userData.joinDate || userData.joinedDate || new Date().toISOString().split("T")[0],
    skills: Array.isArray(userData.skills) ? userData.skills : (userData.skills ? [userData.skills] : []),
    availability: Number(userData.availability) || 100,
    status: typeof userData.isActive === "string" ? userData.isActive : (userData.isActive ? "active" : "inactive"),
    department: userData.department || "Engineering",
    manager: userData.manager || "Super Admin",
    password: userData.password || "admin123",
  };

  const token = localStorage.getItem("authToken");
  const res = await axios.post("/api/v1/employee", payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });

  return {
    status: "success",
    statusCode: res.data?.statusCode || 200,
    statusMessage: res.data?.statusMessage || "Employee created successfully",
    message: "Employee created successfully",
    data: res.data?.data,
  };
}