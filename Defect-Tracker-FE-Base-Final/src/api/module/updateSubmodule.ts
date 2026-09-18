import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const updateSubmodule = async (arg1: number, arg2: number, data: any) => {
  const token = localStorage.getItem("authToken");
  const subName = (data?.name || data?.subModuleName || data?.submoduleName || "").trim();
  const payload = {
    name: subName,
    subModuleName: subName,
    submoduleName: subName,
    description: data?.description || "",
  };

  try {
    const res = await axios.put(ENDPOINTS.subModuleById(arg1, arg2), payload, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    return res.data;
  } catch (err) {
    const res = await axios.put(ENDPOINTS.subModuleById(arg2, arg1), payload, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    return res.data;
  }
};
