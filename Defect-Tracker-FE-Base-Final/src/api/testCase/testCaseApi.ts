import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getTestCasesByProjectAndSubmodule = async (
  _projectId: string,
  subModuleId: string,
  description?: string,
  defectTypeId?: number,
  severityId?: number,
  page?: number,
  size: number = 1000000
): Promise<any[]> => {
  const token = localStorage.getItem("authToken");
  const url = ENDPOINTS.testCaseBySubModule(Number(subModuleId), description, defectTypeId, severityId, page, size);
  const res = await axios.get(url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const data = res.data?.data;
  const items = data?.content || data || [];
  const list = (Array.isArray(items) ? items : []).map((t: any) => ({
    id: t.id,
    no: t.testcaseNo,
    testcaseNo: t.testcaseNo,
    description: t.description,
    detailsSteps: t.detailsSteps || t.steps,
    expectedResult: t.expectedResult,
    subModuleId: t.subModuleId || t.subModule?.id,
    subModuleName: t.subModuleName || t.subModule?.name,
    severityId: t.severityId || t.severity?.id,
    severityName: t.severityName || t.severity?.name,
    defectTypeId: t.defectTypeId || t.defectType?.id,
    defectTypeName: t.defectTypeName || t.defectType?.name,
    createdAt: t.createdAt,
    updatedAt: t.updatedAt,
    createdBy: t.createdBy,
    updatedBy: t.updatedBy,
  }));

  (list as any).totalPages = data?.totalPages || 1;
  (list as any).totalElements = data?.totalElements || list.length;
  (list as any).isServerPaginated = !!data?.totalPages;
  return list;
};

export async function deleteTestCase(
  subModuleId: number,
  testCaseId: string | number
) {
  const token = localStorage.getItem("authToken");
  const res = await axios.delete(ENDPOINTS.testCaseById(subModuleId, Number(testCaseId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return {
    status: 'success',
    statusCode: 200,
    message: res.data?.message || 'Test case deleted successfully',
  };
}

export async function getTestCasesByProjectAndModule(
  projectId: string | number,
  moduleId: string | number,
  page: number = 0,
  size: number = 1000000
) {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(`/api/v1/module/${moduleId}/test-cases?page=${page}&size=${size}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  const data = res.data?.data;
  const items = data?.content || data || [];
  const list = (Array.isArray(items) ? items : []).map((t: any) => ({
    id: t.id,
    no: t.testcaseNo || t.no,
    testcaseNo: t.testcaseNo || t.no,
    description: t.description,
    detailsSteps: t.detailsSteps || t.steps,
    expectedResult: t.expectedResult,
    moduleId: t.moduleId || t.subModule?.module?.id || moduleId,
    moduleName: t.moduleName || t.subModule?.module?.name,
    subModuleId: t.subModuleId || t.subModule?.id,
    subModuleName: t.subModuleName || t.subModule?.name,
    severityId: t.severityId || t.severity?.id,
    severityName: t.severityName || t.severity?.name,
    defectTypeId: t.defectTypeId || t.defectType?.id,
    defectTypeName: t.defectTypeName || t.defectType?.name,
    createdAt: t.createdAt,
    updatedAt: t.updatedAt,
  }));

  (list as any).totalPages = data?.totalPages || 1;
  (list as any).totalElements = data?.totalElements || list.length;
  (list as any).isServerPaginated = !!data?.totalPages;
  return list;
}

export async function getTestCasesByProject(
  projectId: string | number,
  page: number = 0,
  size: number = 1000000
) {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(`/api/v1/project/${projectId}/test-cases?page=${page}&size=${size}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  const data = res.data?.data;
  const items = data?.content || data || [];
  const list = (Array.isArray(items) ? items : []).map((t: any) => ({
    id: t.id,
    no: t.testcaseNo || t.no,
    testcaseNo: t.testcaseNo || t.no,
    description: t.description,
    detailsSteps: t.detailsSteps || t.steps,
    expectedResult: t.expectedResult,
    moduleId: t.moduleId || t.subModule?.module?.id,
    moduleName: t.moduleName || t.subModule?.module?.name,
    subModuleId: t.subModuleId || t.subModule?.id,
    subModuleName: t.subModuleName || t.subModule?.name,
    severityId: t.severityId || t.severity?.id,
    severityName: t.severityName || t.severity?.name,
    defectTypeId: t.defectTypeId || t.defectType?.id,
    defectTypeName: t.defectTypeName || t.defectType?.name,
    createdAt: t.createdAt,
    updatedAt: t.updatedAt,
  }));

  (list as any).totalPages = data?.totalPages || 1;
  (list as any).totalElements = data?.totalElements || list.length;
  (list as any).isServerPaginated = !!data?.totalPages;
  return list;
}

export async function getTestCasesByBulkModules(
  projectId: string | number,
  moduleIds: number[]
) {
  const all: any[] = [];
  for (const modId of moduleIds) {
    const list = await getTestCasesByProjectAndModule(projectId, modId);
    all.push(...list);
  }
  return all;
}

export async function getTestCasesByBulkSubmodules(
  projectId: string | number,
  submoduleIds: number[]
) {
  const all: any[] = [];
  for (const subId of submoduleIds) {
    const list = await getTestCasesByProjectAndSubmodule(String(projectId), String(subId));
    all.push(...list);
  }
  return all;
}
