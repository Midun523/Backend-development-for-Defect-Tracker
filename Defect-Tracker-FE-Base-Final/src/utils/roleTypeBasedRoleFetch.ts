import { getAllRoles } from "../api/role/viewrole";

export type RoleType =
  | "ADMIN"
  | "PROJECT_MANAGER"
  | "QA_LEAD"
  | "QA_ENGINEER"
  | "DEV_LEAD"
  | "SENIOR_DEVELOPER"
  | "DEVELOPER"
  | "JUNIOR_DEVELOPER"
  | "BUSINESS_ANALYST"
  | "UI_UX_DESIGNER"
  | "DEVOPS_ENGINEER"
  | "SUPPORT_ENGINEER"
  | "CLIENT";

const fetchBackendRoles = async (): Promise<Array<{ id: number; roleName: string; type?: string }>> => {
  try {
    const res = await getAllRoles(0, 100);
    const content = res.data?.content || [];
    return content.map((r: any) => ({
      id: r.id,
      roleName: r.name || r.roleName || "",
      type: r.type || r.name,
    }));
  } catch {
    return [
      { id: 1, roleName: "ADMIN", type: "ADMIN" },
      { id: 2, roleName: "PROJECT_MANAGER", type: "PROJECT_MANAGER" },
      { id: 3, roleName: "QA_LEAD", type: "QA_LEAD" },
      { id: 4, roleName: "QA_ENGINEER", type: "QA_ENGINEER" },
      { id: 5, roleName: "DEVELOPER", type: "DEVELOPER" },
    ];
  }
};

export const roleTypeBasedRoleFetch = async (
  roleType: RoleType,
): Promise<string[]> => {
  const roles = await fetchBackendRoles();
  return roles
    .filter((role) => (role.type as string) === roleType || role.roleName?.toUpperCase().includes(roleType.replace('_', ' ')))
    .map((role) => role.roleName);
};

export const roleTypesBasedRoleFetch = async (
  roleTypes: RoleType[],
): Promise<string[]> => {
  const roles = await fetchBackendRoles();
  return roles
    .filter((role) => {
      const typeStr = (role.type || '') as RoleType;
      return roleTypes.includes(typeStr) || roleTypes.some(t => role.roleName?.toUpperCase().includes(t.replace('_', ' ')));
    })
    .map((role) => role.roleName);
};

export const roleTypeBasedRoleIdFetch = async (
  roleType: RoleType,
): Promise<number[]> => {
  const roles = await fetchBackendRoles();
  return roles
    .filter((role) => (role.type as string) === roleType || role.roleName?.toUpperCase().includes(roleType.replace('_', ' ')))
    .map((role) => role.id);
};