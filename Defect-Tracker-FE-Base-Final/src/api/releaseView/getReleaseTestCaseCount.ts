import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const getReleaseTestCaseCount = async (releaseId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(ENDPOINTS.releaseTestCase(Number(releaseId)), {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  const items = res.data?.data;
  const count = Array.isArray(items) ? items.length : (items?.totalElements || 0);
  return count;
};

export const getReleaseTestCaseCounts = async (projectIdOrReleaseId?: any) => {
  return {
    data: {},
  };
};
