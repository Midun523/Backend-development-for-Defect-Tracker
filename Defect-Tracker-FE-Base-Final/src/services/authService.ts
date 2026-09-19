import apiClient, { tokenManager } from '../lib/api';
import { ENDPOINTS } from '../utils/apiendpoint';

interface LoginResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: {
    token: string;
    refreshToken: string;
    type: string;
    userId: number;
    employeeId: number | null;
    companyStaffId: number | null;
    email: string;
    firstName: string;
    lastName: string;
    userType: string;
    roles: string[];
    globalPermissions: string[];
    projectAccessList: string[];
  };
}

const clearAuthDataHelper = (): void => {
  tokenManager.removeToken();
  tokenManager.removeRefreshToken();
  localStorage.removeItem('user');
  localStorage.removeItem('auth_token');
  localStorage.removeItem('authToken');
  sessionStorage.removeItem('auth_token');

  // Note: protected UI state keys (statusWorkflowNodes, statusWorkflowEdges, emailConfigTab, assignments, selectedProjectId)
  // are intentionally preserved and NOT cleared here.
};

class AuthService {
  static async login(email: string, password?: string): Promise<LoginResponse> {
    this.clearAuthData();

    const response = await apiClient.post(ENDPOINTS.login, {
      username: email,
      password: password || 'admin123',
    });

    if (response.data?.data?.token) {
      const data = response.data.data;
      const responseData: LoginResponse = {
        status: 'success',
        statusCode: 200,
        statusMessage: 'Login successful',
        data: {
          token: data.token,
          refreshToken: data.refreshToken || '',
          type: data.type || 'Bearer',
          userId: data.userId,
          employeeId: data.employeeId,
          companyStaffId: data.companyStaffId,
          email: data.email,
          firstName: data.firstName,
          lastName: data.lastName,
          userType: data.userType || 'CompanyStaff',
          roles: data.roles || ['Super Admin'],
          globalPermissions: data.globalPermissions || ['ALL_PERMISSIONS'],
          projectAccessList: data.projectAccessList || [],
        },
      };

      const { token, refreshToken, ...userData } = responseData.data;
      tokenManager.setToken(token);
      tokenManager.setRefreshToken(refreshToken);
      localStorage.setItem('user', JSON.stringify(userData));
      return responseData;
    }

    throw new Error('Authentication failed: Invalid credentials or empty token response.');
  }

  static async changePassword(currentPassword?: string, newPassword?: string, confirmPassword?: string): Promise<void> {
    await apiClient.post(ENDPOINTS.changePassword, {
      currentPassword,
      newPassword,
      confirmPassword,
    });
  }

  static async logout(): Promise<void> {
    try {
      await apiClient.post(ENDPOINTS.logout);
    } catch {
      // Ignore network errors on logout
    } finally {
      this.clearAuthData();
    }
  }

  static clearAuthData(): void {
    clearAuthDataHelper();
  }

  static logoutImmediate(): void {
    this.clearAuthData();
  }

  static isAuthenticated(): boolean {
    const token = tokenManager.getToken();
    return token !== null && token.length > 0;
  }

  static getCurrentUser(): any | null {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  }

  static getToken(): string | null {
    return tokenManager.getToken();
  }
}

export default AuthService;