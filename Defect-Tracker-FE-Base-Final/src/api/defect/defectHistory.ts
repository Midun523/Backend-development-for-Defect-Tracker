import axios from "axios";

export interface DefectHistoryEntry {
  id?: number;
  defectId?: number;
  action?: string;
  fieldChanged?: string;
  oldValue?: string;
  newValue?: string;
  changedBy?: string;
  changedByName?: string;
  changedAt?: string;
  createdAt?: string;
  timestamp?: string;
  [key: string]: any;
}

export const getDefectHistory = async (defectId: number | string) => {
  const token = localStorage.getItem("authToken");
  const res = await axios.get(`/api/v1/defect/${defectId}/history`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }).catch(() => ({ data: { data: [] } }));
  return res.data?.data || [];
};

export const getDefectHistoryByDefectId = getDefectHistory;
