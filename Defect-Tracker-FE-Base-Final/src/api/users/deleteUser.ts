import axios from "axios";

export async function deleteUser(id: number | string) {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(`/api/v1/employee/${id}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: "success",
    statusCode: res.data?.statusCode || 200,
    statusMessage: res.data?.message || "Employee deleted successfully",
    message: "Employee deleted successfully",
  };
}