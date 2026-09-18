import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export type ExecutionStatus =
  | "not-started"
  | "in-progress"
  | "passed"
  | "failed"
  | "blocked";

const EXECUTION_STATUS_KEY = "executionStatuses";

export function getExecutionStatuses(
  projectId: string | number,
  releaseId: string | number
): Record<string, ExecutionStatus> {
  try {
    const raw = localStorage.getItem(EXECUTION_STATUS_KEY);
    if (!raw) return {};
    const all: Record<string, any> = JSON.parse(raw);
    const proj = all[String(projectId)] || {};
    return (proj[String(releaseId)] || {}) as Record<string, ExecutionStatus>;
  } catch {
    return {};
  }
}

export function setExecutionStatus(
  projectId: string | number,
  releaseId: string | number,
  testCaseId: string | number,
  status: ExecutionStatus
): Record<string, ExecutionStatus> {
  let all: Record<string, any> = {};
  try {
    const raw = localStorage.getItem(EXECUTION_STATUS_KEY);
    all = raw ? JSON.parse(raw) : {};
  } catch {
    all = {};
  }

  const pid = String(projectId);
  const rid = String(releaseId);
  if (!all[pid]) all[pid] = {};
  if (!all[pid][rid]) all[pid][rid] = {};
  all[pid][rid][String(testCaseId)] = status;

  localStorage.setItem(EXECUTION_STATUS_KEY, JSON.stringify(all));

  return all[pid][rid] as Record<string, ExecutionStatus>;
}

export function setBulkExecutionStatuses(
  projectId: string | number,
  releaseId: string | number,
  statuses: Record<string, ExecutionStatus>
): void {
  let all: Record<string, any> = {};
  try {
    const raw = localStorage.getItem(EXECUTION_STATUS_KEY);
    all = raw ? JSON.parse(raw) : {};
  } catch {
    all = {};
  }

  const pid = String(projectId);
  const rid = String(releaseId);
  if (!all[pid]) all[pid] = {};
  all[pid][rid] = { ...(all[pid][rid] || {}), ...statuses };
  localStorage.setItem(EXECUTION_STATUS_KEY, JSON.stringify(all));
}

export const updateReleaseTestCaseStatus = async (
  releaseId: number,
  releaseTestCaseId: number,
  payload: {
    status: "PASSED" | "FAILED";
    priorityId?: number;
    assignedTo?: number;
  }
): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.patch(
    ENDPOINTS.releaseTestCaseStatus(releaseId, releaseTestCaseId),
    payload,
    { headers: token ? { Authorization: `Bearer ${token}` } : {} }
  );
  return {
    status: 'success',
    statusCode: 200,
    message: res.data?.message || 'Test case status updated successfully',
    data: res.data?.data,
  };
};

export const updateReleaseTestCaseStatusWithImage = async (
  releaseId: number,
  releaseTestCaseId: number,
  formData: FormData
): Promise<any> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.patch(
    ENDPOINTS.releaseTestCaseStatus(releaseId, releaseTestCaseId),
    formData,
    {
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        'Content-Type': 'multipart/form-data',
      },
    }
  );
  return {
    status: 'success',
    statusCode: 200,
    message: res.data?.message || 'Test case status updated successfully',
    data: res.data?.data,
  };
};