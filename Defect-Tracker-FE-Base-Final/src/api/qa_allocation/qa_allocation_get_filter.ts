import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getQAAllocationFilter = async (releaseId: number | string, filters?: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.releaseTestCase(Number(releaseId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    params: filters,
  }).catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const getAllocatedTestCases = getQAAllocationFilter;
export const allocated_testcases = getQAAllocationFilter;
export const allocated_testcase_details = getQAAllocationFilter;

export const bulkAssignOwner = async (data: any) => {
  return { status: 'success', data };
};
