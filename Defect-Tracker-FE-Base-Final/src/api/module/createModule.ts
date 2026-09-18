import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const createModule = async (projectIdOrData: any, data?: any) => {
  const token = localStorage.getItem("authToken");
  let pId = projectIdOrData;
  let body = data;
  if (typeof projectIdOrData === "object" && projectIdOrData !== null && !data) {
    pId = projectIdOrData.projectId || projectIdOrData.project_id;
    body = projectIdOrData;
  }
  const payload = {
    name: (body?.name || body?.moduleName || "").trim(),
    moduleName: (body?.moduleName || body?.name || "").trim(),
    description: body?.description || "",
    projectId: Number(pId),
    project_id: Number(pId),
    leaderId: body?.leaderId || body?.leader_id,
  };
  const res = await axios.post(ENDPOINTS.module(Number(pId)), payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const createSubModule = async (moduleIdOrData: any, data?: any) => {
  const token = localStorage.getItem("authToken");
  let mId = moduleIdOrData;
  let body = data;
  if (typeof moduleIdOrData === "object" && moduleIdOrData !== null && !data) {
    mId = moduleIdOrData.moduleId || moduleIdOrData.module_id;
    body = moduleIdOrData;
  }
  const subName = (body?.name || body?.subModuleName || body?.submoduleName || "").trim();
  const payload = {
    name: subName,
    subModuleName: subName,
    submoduleName: subName,
    description: body?.description || "",
    moduleId: Number(mId),
    module_id: Number(mId),
  };
  const res = await axios.post(ENDPOINTS.subModule(Number(mId)), payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const createSubmodule = createSubModule;
