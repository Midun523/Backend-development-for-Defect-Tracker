import axios from "axios";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface ForgotPasswordResponse {
  status: string;
  statusMessage: string;
  data: any;
  statusCode: number;
}

export async function forgotPassword(email: string): Promise<ForgotPasswordResponse> {
  try {
    const res = await axios.post(ENDPOINTS.forgetPassword, { email });
    return {
      status: res.data?.status || 'success',
      statusMessage: res.data?.message || res.data?.statusMessage || 'Password reset link sent to your email',
      data: res.data?.data || null,
      statusCode: res.data?.statusCode || res.status || 200,
    };
  } catch (error: any) {
    const msg = error.response?.data?.message || error.message || 'Failed to send reset instructions';
    const code = error.response?.status || 400;
    return {
      status: 'error',
      statusMessage: msg,
      data: null,
      statusCode: code,
    };
  }
}

export async function resetPassword(token: string, newPassword: string): Promise<ForgotPasswordResponse> {
  try {
    const res = await axios.post(ENDPOINTS.resetPassword, { token, newPassword });
    return {
      status: res.data?.status || 'success',
      statusMessage: res.data?.message || res.data?.statusMessage || 'Password reset successfully',
      data: res.data?.data || null,
      statusCode: res.data?.statusCode || res.status || 200,
    };
  } catch (error: any) {
    const msg = error.response?.data?.message || error.message || 'Failed to reset password';
    const code = error.response?.status || 400;
    return {
      status: 'error',
      statusMessage: msg,
      data: null,
      statusCode: code,
    };
  }
}

