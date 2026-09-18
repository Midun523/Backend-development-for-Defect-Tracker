import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const normalizeBenchEmployee = (item: any) => {
  const desig =
    item.designation?.designationName ||
    item.designation ||
    item.designationName ||
    "Software Engineer";
  const desigStr = typeof desig === "object" ? desig.designationName || "Software Engineer" : String(desig);
  const isActive =
    item.status !== undefined
      ? String(item.status).toLowerCase() === "active"
      : item.active !== false;
  const avail = item.availability !== undefined ? Number(item.availability) : 100;
  const idStr = String(item.id || item.employeeId || "");

  return {
    id: idStr,
    firstName: item.firstName || "",
    lastName: item.lastName || "",
    email: item.email || "",
    phone: item.phone || item.contactNo || "",
    contactNo: item.phone || item.contactNo || "",
    designation: desigStr,
    designationName: desigStr,
    availability: avail,
    availabilityPeriod: item.availabilityPeriod || "Immediate",
    status: isActive ? "active" : "inactive",
    active: isActive,
    employee: {
      id: idStr,
      firstName: item.firstName || "",
      lastName: item.lastName || "",
      email: item.email || "",
      contactNo: item.phone || item.contactNo || "",
      phone: item.phone || item.contactNo || "",
      designationName: desigStr,
      designation: desigStr,
      active: isActive,
      status: isActive ? "active" : "inactive",
      availability: avail,
    },
  };
};

export const getBenchEmployees = async () => {
  const token = localStorage.getItem("authToken");
  try {
    const res = await axios.get(ENDPOINTS.benchEmployee, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const list = res.data?.data || res.data || [];
    const normalized = (Array.isArray(list) ? list : []).map(normalizeBenchEmployee);
    // Bench strictly requires active status and availability > 0
    return normalized.filter((e) => e.active && e.availability > 0);
  } catch (error) {
    console.error("Failed to fetch bench employees, falling back to users:", error);
    try {
      const res = await axios.get(ENDPOINTS.getAllUsers, {
        params: { size: 1000 },
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      const users = res.data?.data?.content || res.data?.data || [];
      return (Array.isArray(users) ? users : [])
        .map(normalizeBenchEmployee)
        .filter((e) => e.active && e.availability > 0);
    } catch {
      return [];
    }
  }
};

export const getBenchList = getBenchEmployees;

export const getEmployeeProjectHistory = async (employeeId: string | number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios
    .get(ENDPOINTS.projectAllocationByEmployee(Number(employeeId)), {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
    .catch(() => ({ data: { data: [] } }));
  return res.data;
};

export const getEmployeeDetails = async (employeeId: string | number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios
    .get(ENDPOINTS.getUserById(Number(employeeId)), {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
    .catch(() => ({ data: { data: null } }));
  return res.data?.data || res.data;
};

export const getBenchAvailability = async (
  _page: number = 0,
  _size: number = 1000,
  _filters: any = {}
) => {
  const employees = await getBenchEmployees();
  return {
    data: {
      data: employees,
      content: employees,
      totalElements: employees.length,
      totalPages: Math.ceil(employees.length / (_size || 10)),
    },
  };
};
