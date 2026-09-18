import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export async function getTestCasesByFilter({
  projectId,
  releaseId,
  moduleId,
  subModuleId,
}: {
  projectId: number;
  releaseId: number;
  moduleId?: number;
  subModuleId?: number;
}) {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.releaseTestCase(releaseId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    params: { moduleId, subModuleId },
  });
  const data = res.data?.data;
  const items = data?.content || data || [];
  return (Array.isArray(items) ? items : []).map((t: any) => ({
    id: t.id,
    testcaseNo: t.testcaseNo,
    description: t.description,
    detailsSteps: t.detailsSteps || t.steps,
    expectedResult: t.expectedResult,
    severityName: t.severityName || t.severity?.name,
    defectTypeName: t.defectTypeName || t.defectType?.name,
    subModuleName: t.subModuleName || t.subModule?.name,
    moduleName: t.moduleName || t.module?.name,
    projectId,
    releaseId,
    executionStatus: t.executionStatus || 'NOT_RUN',
  }));
}