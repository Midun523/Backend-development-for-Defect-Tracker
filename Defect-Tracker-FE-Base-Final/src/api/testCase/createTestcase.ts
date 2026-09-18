import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface CreateTestCaseRequest {
  description: string;
  detailsSteps: string;
  expectedResult?: string;
  severityId: number;
  defectTypeId: number;
}

export interface CreateTestCaseResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: any;
}

export async function createTestCase(subModuleId: number, testCaseData: CreateTestCaseRequest) {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.testCaseBySubModule(subModuleId), testCaseData, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: res.data?.status || 'success',
    statusCode: res.data?.statusCode || res.status || 201,
    message: res.data?.message || res.data?.statusMessage || 'Test case created successfully',
    statusMessage: res.data?.message || res.data?.statusMessage || 'Test case created successfully',
    data: res.data?.data,
  };
}

export const createTestCaseSub = async (subModuleId: number, payload: CreateTestCaseRequest): Promise<CreateTestCaseResponse> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.post(ENDPOINTS.testCaseBySubModule(subModuleId), payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: res.data?.status || 'success',
    statusCode: res.data?.statusCode || res.status || 201,
    statusMessage: res.data?.message || res.data?.statusMessage || 'Test case created successfully',
    data: res.data?.data,
  };
};
