import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const searchTestCaseByCriteria = async (
  subModuleId: number,
  description?: string,
  defectTypeId?: number,
  severityId?: number
) => {
  const token = localStorage.getItem("authToken");
  const url = ENDPOINTS.testCaseBySubModule(subModuleId, description, defectTypeId, severityId);
  const res = await axios.get(url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const data = res.data?.data;
  const items = data?.content || data || [];
  return (Array.isArray(items) ? items : []).map((t: any) => ({
    id: t.id,
    testcaseNo: t.testcaseNo,
    description: t.description,
    detailsSteps: t.detailsSteps || t.steps,
    expectedResult: t.expectedResult,
    severityId: t.severityId || t.severity?.id,
    severityName: t.severityName || t.severity?.name,
    defectTypeId: t.defectTypeId || t.defectType?.id,
    defectTypeName: t.defectTypeName || t.defectType?.name,
    subModuleId: t.subModuleId || t.subModule?.id,
    subModuleName: t.subModuleName || t.subModule?.name,
  }));
};