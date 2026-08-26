import api from './api';
import type { LoginRequest, RegisterRequest } from './types';

export const authService = {
  login: async (credentials: LoginRequest) => {
    const response = await api.post('/auth/login', credentials);
    return response.data; // Expected { accessToken: string }
  },
  register: async (data: RegisterRequest) => {
    await api.post('/auth/register', data);
  },
};
