import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export async function updateTestCase(
  subModuleId: number,
  testCaseId: string | number,
  data: any
) {
  const token = localStorage.getItem("authToken");
  const res = await axios.put(ENDPOINTS.testCaseById(subModuleId, Number(testCaseId)), {
    description: data.description,
    detailsSteps: data.detailsSteps || data.steps,
    expectedResult: data.expectedResult,
    severityId: data.severityId,
    defectTypeId: data.defectTypeId,
  }, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });

  return {
    status: 'success',
    statusCode: 200,
    message: res.data?.message || 'Test case updated successfully',
    data: res.data?.data,
  };
}
