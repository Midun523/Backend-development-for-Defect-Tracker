import axios from 'axios';
import { tokenManager } from '../lib/api';
import { mockDb } from '../mock/mockData';

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

  const APP_STORAGE_KEYS = [
    'statusWorkflowNodes',
    'statusWorkflowEdges',
    'emailConfigTab',
    'assignments',
    'selectedProjectId'
  ];

  APP_STORAGE_KEYS.forEach((key) => {
    localStorage.removeItem(key);
    sessionStorage.removeItem(key);
  });
};

class AuthService {
  static async login(email: string, password?: string): Promise<LoginResponse> {
    this.clearAuthData();

    try {
      const response = await axios.post('/api/v1/auth/login', {
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
    } catch (err) {
      console.warn('Backend login endpoint error, falling back to local session:', err);
    }

    // Fallback default admin
    const users = mockDb.getUsers();
    const matchedUser = users.find(u => u.email.toLowerCase() === email.toLowerCase()) || users[0];

    const mockToken = 'mock_jwt_token_' + btoa(JSON.stringify({
      sub: matchedUser?.email || 'admin@defecttracker.com',
      userId: matchedUser?.id || 1,
      exp: Math.floor(Date.now() / 1000) + (365 * 24 * 3600),
      userType: matchedUser?.userType || 'CompanyStaff'
    }));

    const mockRefreshToken = 'mock_refresh_token_' + Date.now();

    const responseData: LoginResponse = {
      status: 'success',
      statusCode: 200,
      statusMessage: 'Login successful',
      data: {
        token: mockToken,
        refreshToken: mockRefreshToken,
        type: 'Bearer',
        userId: matchedUser?.id || 1,
        employeeId: matchedUser?.id || 1,
        companyStaffId: matchedUser?.id || 1,
        email: matchedUser?.email || 'admin@defecttracker.com',
        firstName: matchedUser?.firstName || 'Super',
        lastName: matchedUser?.lastName || 'Admin',
        userType: matchedUser?.userType || 'CompanyStaff',
        roles: matchedUser?.roles || ['Super Admin'],
        globalPermissions: ['ALL_PERMISSIONS'],
        projectAccessList: ['1', '2', '3'],
      },
    };

    const { token, refreshToken, ...userData } = responseData.data;

    tokenManager.setToken(token);
    tokenManager.setRefreshToken(refreshToken);
    localStorage.setItem('user', JSON.stringify(userData));

    return responseData;
  }

  static async changePassword(_currentPassword?: string, _newPassword?: string, _confirmPassword?: string): Promise<void> {
    // Return mock success
    return Promise.resolve();
  }

  static async logout(): Promise<void> {
    this.clearAuthData();
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
    // Fallback default admin user
    const defaultUser = mockDb.getUsers()[0];
    const userObj = {
      userId: defaultUser.id,
      employeeId: defaultUser.id,
      companyStaffId: defaultUser.id,
      email: defaultUser.email,
      firstName: defaultUser.firstName,
      lastName: defaultUser.lastName,
      userType: 'CompanyStaff',
      roles: ['Super Admin'],
      globalPermissions: ['ALL_PERMISSIONS'],
      projectAccessList: ['1', '2', '3'],
    };
    return userObj;
  }

  static getToken(): string | null {
    return tokenManager.getToken();
  }
}

export default AuthService;