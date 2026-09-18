import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const deleteProject = async (id: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.projectById(id), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};
