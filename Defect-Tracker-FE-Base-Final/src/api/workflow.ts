import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";

interface WorkflowNodeRequest {
  id: number;
  positionX: number;
  positionY: number;
}

interface WorkflowConnectionRequest {
  fromStatusId: number;
  toStatusId: number;
}

export interface SaveWorkflowRequest {
  nodes: WorkflowNodeRequest[];
  connections: WorkflowConnectionRequest[];
}

export interface SaveWorkflowResponse {
  status: string;
  statusMessage: string;
  data?: any;
  statusCode: number;
}

interface StatusInfo {
  id: number;
  name: string;
  color: string;
}

interface WorkflowTransitionResponse {
  id: number;
  fromStatus: StatusInfo;
  toStatus: StatusInfo;
}

export interface GetAllWorkflowsResponse {
  status: string;
  statusMessage: string;
  data: WorkflowTransitionResponse[];
  statusCode: number;
}

export interface NextStatusResponse {
  status: string;
  statusMessage: string;
  data: StatusInfo[];
  statusCode: number;
}

export const getAllWorkflows = async (): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.workflow, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    ...res.data,
    statusMessage: res.data?.statusMessage || res.data?.message || "Success",
    data: res.data?.data || [],
  };
};

export const saveWorkflow = async (workflowData: SaveWorkflowRequest): Promise<SaveWorkflowResponse> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.workflow, workflowData, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    ...res.data,
    statusMessage: res.data?.statusMessage || res.data?.message || "Workflow saved successfully",
    data: res.data?.data,
  };
};

export const getNextStatuses = async (
  fromStatusId: number
): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.workflowNextStatus(fromStatusId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const list = res.data?.data || res.data || [];
  const normalized = (Array.isArray(list) ? list : []).map((s: any) => {
    const rawColor = s.color || s.colorCode || "#6B7280";
    const hex = rawColor.startsWith("#") ? rawColor : `#${rawColor}`;
    return {
      ...s,
      id: s.id,
      name: s.name || s.statusName || "",
      color: hex,
      colorCode: hex,
    };
  });
  return {
    ...res.data,
    statusMessage: res.data?.statusMessage || res.data?.message || "Success",
    data: normalized,
  };
};

