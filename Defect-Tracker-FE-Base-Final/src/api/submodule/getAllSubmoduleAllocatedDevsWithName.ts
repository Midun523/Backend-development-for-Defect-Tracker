import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface SubDevWithName {
  id: number;
  employeeId: number;
  submoduleId: number;
  employeeName?: string;
}

export interface SubDevWithNameResponse {
  status: number;
  statusCode: string;
  statusMessage: string;
  data: SubDevWithName[];
}

export const getAllSubDevwithName = async (
  submoduleId: number
): Promise<SubDevWithNameResponse> => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.subModuleDev(submoduleId), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  const items = res.data?.data || [];
  return {
    status: 200,
    statusCode: '200',
    statusMessage: 'Success',
    data: (Array.isArray(items) ? items : []).map((a: any) => ({
      id: a.id,
      employeeId: a.employeeId || a.employee?.id,
      submoduleId,
      employeeName: a.employeeName || (a.employee ? `${a.employee.firstName} ${a.employee.lastName}` : 'Developer'),
    })),
  };
};

export default getAllSubDevwithName;