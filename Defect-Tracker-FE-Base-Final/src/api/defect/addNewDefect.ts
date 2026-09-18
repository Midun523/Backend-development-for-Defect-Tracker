import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const addNewDefect = async (defectData: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.defect, defectData, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const addDefects = addNewDefect;
