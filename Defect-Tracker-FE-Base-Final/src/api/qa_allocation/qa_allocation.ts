import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface QAMember {
  id?: number;
  userId?: number;
  employeeId?: number;
  name?: string;
  roleName?: string;
  [key: string]: any;
}

export const allocateQA = async (releaseId: number, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.releaseTestCaseQaAllocation(releaseId), data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const assignQAToTestCase = async (releaseId: number, testCaseId: number, data: any) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.releaseTestCaseQaAssign(releaseId, testCaseId), data, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return res.data;
};

export const getQAMembersByProjectId = async (projectId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.getAllUsers, {
    params: { size: 1000 },
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  const users = res.data?.data?.content || res.data?.data || [];
  return {
    data: users.map((u: any) => ({
      id: u.id,
      userId: u.id,
      name: `${u.firstName || ''} ${u.lastName || ''}`.trim() || 'QA Member',
      roleName: u.roleName || 'QA',
    })),
  };
};
