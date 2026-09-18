import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const createComment = async (defectId: number, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.defectComment(defectId), data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const updateComment = async (commentId: number, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(`${ENDPOINTS.defect}/comment/${commentId}`, data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { status: 'success' } }));
  return res.data;
};
