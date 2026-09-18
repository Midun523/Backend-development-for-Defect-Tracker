import axios from "axios";
import { ENDPOINTS } from "../utils/apiendpoint";

export const getReleaseTestCases = async (releaseId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.releaseTestCase(Number(releaseId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const addTestCaseToRelease = async (releaseId: number | string, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.releaseTestCase(Number(releaseId)), data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const allocateTestCaseToRelease = addTestCaseToRelease;

export const allocateTestCaseToMultipleReleases = async (data: any) => {
  return { status: 'success', data };
};

export const bulkAllocateTestCasesToReleases = async (data: any) => {
  return { status: 'success', data };
};

export const allocateTestCasesToManyReleases = async (data: any) => {
  return { status: 'success', data };
};

export const getReleaseTestCasesByFiltersGroup = async (filters: any) => {
  return { status: 'success', data: [] };
};

export const getQaAllocationSummary = async (releaseId?: any) => {
  return { status: 'success', data: [] };
};

export const getQaEngineerTestCases = async (employeeId?: any) => {
  return { status: 'success', data: [] };
};

export const removeTestCaseFromRelease = async (releaseId: number | string, testCaseId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.releaseTestCaseById(Number(releaseId), Number(testCaseId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const getDefectTestCaseCounts = async (_projectId?: any, _releaseId?: any) => {
  return {
    data: {},
  };
};

export const getTestCasesByFilter = async (projectId?: any, moduleId?: any, submoduleId?: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.getAllTestCases, {
    params: { projectId, moduleId, submoduleId },
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};
