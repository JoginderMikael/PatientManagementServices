import { apiRequest } from '../api/client';

export interface LoginRequest {
  email: string;
  password: string;
}

interface LoginResponse {
  token: string;
}

export function loginRequest(credentials: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse, LoginRequest>('/auth/login', { method: 'POST', body: credentials, notifyOnUnauthorized: false });
}

export function validateToken(token: string): Promise<void> {
  return apiRequest<void>('/auth/validate', { token, responseType: 'empty' });
}
