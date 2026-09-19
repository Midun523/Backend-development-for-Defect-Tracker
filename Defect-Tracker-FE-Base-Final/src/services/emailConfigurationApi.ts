import { SmtpConfig } from "../types/emailConfiguration";
import apiClient from "../lib/api";
import { ENDPOINTS } from "../utils/apiendpoint";
import { INITIAL_EMAIL_POINT_SETUPS, INITIAL_EMAIL_TEMPLATES } from "../mock/mockData";

export interface CreateSmtpConfigRequest {
  name: string;
  smtpHost: string;
  smtpPort: number;
  username: string;
  password: string;
  fromEmail: string;
  fromName: string;
  isEnabled?: boolean;
}

export const createSmtpConfig = async (data: CreateSmtpConfigRequest): Promise<SmtpConfig> => {
  const res = await apiClient.post(ENDPOINTS.emailConfig, data);
  return (res.data?.data || res.data) as SmtpConfig;
};

export const getSmtpConfigs = async (): Promise<SmtpConfig[]> => {
  const res = await apiClient.get(ENDPOINTS.emailConfig);
  const data = res.data?.data || res.data || [];
  return Array.isArray(data) ? data : [];
};

export const updateSmtpConfig = async (id: number, data: CreateSmtpConfigRequest): Promise<SmtpConfig> => {
  const res = await apiClient.put(ENDPOINTS.emailConfigById(id), data);
  return (res.data?.data || res.data) as SmtpConfig;
};

export const deleteSmtpConfig = async (id: number): Promise<void> => {
  await apiClient.delete(ENDPOINTS.emailConfigById(id));
};

export const updateSmtpConfigStatus = async (id: number, isEnabled: boolean) => {
  const res = await apiClient.patch(ENDPOINTS.emailConfigEnable(id), { isEnabled });
  return {
    status: 'success',
    statusCode: 200,
    data: res.data?.data || res.data || { id, isEnabled },
  };
};

export const getAllEmailPointSetups = async () => {
  try {
    const res = await apiClient.get(ENDPOINTS.emailPointSetup);
    return res.data?.data || res.data || INITIAL_EMAIL_POINT_SETUPS;
  } catch {
    return INITIAL_EMAIL_POINT_SETUPS;
  }
};

export const getRoleNotificationChannels = async (roleId: number): Promise<Record<number, string>> => {
  try {
    const res = await apiClient.get(ENDPOINTS.roleAssignedPoints(roleId));
    return res.data?.data || res.data || {};
  } catch {
    return {
      1: 'email',
      2: 'email',
      3: 'in-app',
      4: 'email',
    };
  }
};

export const updateRoleNotificationRules = async (roleId: number, pointChannels: Map<number, string>): Promise<void> => {
  const channelsObj: Record<string, string> = {};
  pointChannels.forEach((val, key) => {
    channelsObj[key.toString()] = val;
  });
  await apiClient.post(ENDPOINTS.roleNotificationUpdate, { roleId, channels: channelsObj });
};

export const updateUserExtraPoints = async (userId: number, pointChannels: Map<number, string>): Promise<void> => {
  const channelsObj: Record<string, string> = {};
  pointChannels.forEach((val, key) => {
    channelsObj[key.toString()] = val;
  });
  await apiClient.post(ENDPOINTS.userExtraRulesUpdate, { userId, channels: channelsObj });
};

export const updateEmailPointSetupStatus = async (id: number, isEnabled: boolean) => {
  return {
    status: 'success',
    statusCode: 200,
    data: { id, isEnabled },
  };
};

export const getAllEmailTemplates = async () => {
  try {
    const res = await apiClient.get(ENDPOINTS.emailTemplate);
    return {
      status: 'success',
      statusCode: 200,
      data: res.data?.data || res.data || INITIAL_EMAIL_TEMPLATES,
    };
  } catch {
    return {
      status: 'success',
      statusCode: 200,
      data: INITIAL_EMAIL_TEMPLATES,
    };
  }
};

export const updateEmailTemplate = async (id: number, data: { subject: string; body: string }) => {
  const res = await apiClient.put(ENDPOINTS.emailTemplateById(id), data);
  return {
    status: 'success',
    statusCode: 200,
    data: res.data?.data || res.data || { id, ...data },
  };
};

export const resetEmailTemplate = async (id: number) => {
  const res = await apiClient.patch(ENDPOINTS.emailTemplateReset(id));
  return {
    status: 'success',
    statusCode: 200,
    data: res.data?.data || res.data,
  };
};