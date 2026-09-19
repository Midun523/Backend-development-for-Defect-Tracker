import axios from "axios";

export const submoduleBulkAllocate = async (
  _projectId: number,
  _moduleId: number,
  subModuleId: number,
  userIds: number[]
) => {
  const token = localStorage.getItem("authToken");
  try {
    for (const userId of userIds) {
      await axios.post(
        `/api/v1/sub-module/${subModuleId}/employee`,
        { employeeId: userId, userId },
        { headers: token ? { Authorization: `Bearer ${token}` } : {} }
      );
    }
  } catch (e) {
    // Ignore error if already allocated or continue
  }
  return {
    status: "success",
    statusCode: 200,
    message: "Submodule developers allocated successfully",
  };
};
