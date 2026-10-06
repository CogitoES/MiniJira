import { createContext } from 'react';
import type { LoginRequest } from './types';

export interface AuthContextType {
  accessToken: string | null;
  refreshToken: string | null;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  setAccessToken: (token: string | null) => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
