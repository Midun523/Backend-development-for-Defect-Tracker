import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const updateDefect = async (defectId: number | string, defectData: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.defectById(Number(defectId)), defectData, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const updateDefectById = updateDefect;
