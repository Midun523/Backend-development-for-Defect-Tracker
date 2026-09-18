import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const deleteRelease = async (id: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.releaseById(Number(id)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const deleteReleaseById = deleteRelease;
