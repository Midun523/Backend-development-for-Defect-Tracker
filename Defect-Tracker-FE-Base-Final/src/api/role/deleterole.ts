import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const deleterole = async (id: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.roleById(id), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};
