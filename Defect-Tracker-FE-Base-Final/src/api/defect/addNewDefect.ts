import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const addNewDefect = async (defectData: any) => {
  const token = localStorage.getItem("authToken");
  const headers: any = token ? { Authorization: `Bearer ${token}` } : {};
  if (typeof FormData !== "undefined" && defectData instanceof FormData) {
    headers["Content-Type"] = "multipart/form-data";
  }
  const res = await axios.post(ENDPOINTS.defect, defectData, { headers });
  return res.data;
};

export const addDefects = addNewDefect;
