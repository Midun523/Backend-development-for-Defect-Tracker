import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const deleteDefect = async (defectId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.defectById(Number(defectId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const deleteDefectById = deleteDefect;
