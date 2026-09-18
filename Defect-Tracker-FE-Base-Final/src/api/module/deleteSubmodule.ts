import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const deleteSubmodule = async (arg1: number, arg2?: number) => {
  const token = localStorage.getItem("authToken");
  if (arg2 !== undefined && arg2 !== null) {
    try {
      const res = await axios.delete(ENDPOINTS.subModuleById(arg1, arg2), {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      return res.data;
    } catch {
      const res = await axios.delete(ENDPOINTS.subModuleById(arg2, arg1), {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      return res.data;
    }
  } else {
    // Single ID delete
    const res = await axios.delete(`${ENDPOINTS.module(1).replace(/\/project\/1\/module/, '')}/sub-module/${arg1}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    return res.data;
  }
};
