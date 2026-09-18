import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";

export const getEmailConfigs = async () => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.emailConfig, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data?.data || [];
};

export const updateEmailConfig = async (id: number, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.emailConfigById(id), data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const createEmailConfig = async (data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.emailConfig, data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const enableEmailConfig = async (id: number) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.emailConfigEnable(id), {}, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};
