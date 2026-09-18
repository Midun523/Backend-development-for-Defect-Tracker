import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getCommentsByDefect = async (defectId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.defectComment(Number(defectId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const getCommentsByDefectId = getCommentsByDefect;
