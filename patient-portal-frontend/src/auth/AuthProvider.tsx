import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { setUnauthorizedHandler } from '../api/client';
import { loginRequest, validateToken, LoginRequest } from './authApi';
import { AuthSession, clearSession, readSession, writeSession } from './session';
import { parseTokenClaims } from './tokenClaims';

type AuthStatus = 'restoring' | 'anonymous' | 'authenticated';

interface AuthContextValue {
  status: AuthStatus;
  session: AuthSession | null;
  login(credentials: LoginRequest): Promise<AuthSession>;
  logout(reason?: 'manual' | 'expired'): void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const cached = readSession();
  const [session, setSession] = useState<AuthSession | null>(cached);
  const [status, setStatus] = useState<AuthStatus>(cached ? 'restoring' : 'anonymous');
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const purgeSession = useCallback((reason: 'manual' | 'expired' = 'manual') => {
    clearSession();
    setSession(null);
    setStatus('anonymous');
    queryClient.clear();
    navigate('/login', { replace: true, state: reason === 'expired' ? { reason: 'expired' } : undefined });
  }, [navigate, queryClient]);

  useEffect(() => {
    setUnauthorizedHandler(() => purgeSession('expired'));
    return () => setUnauthorizedHandler(null);
  }, [purgeSession]);

  useEffect(() => {
    if (!cached) return;
    let active = true;
    validateToken(cached.token)
      .then(() => { if (active) setStatus('authenticated'); })
      .catch(() => { if (active) purgeSession('expired'); });
    return () => { active = false; };
  // The cached credential is read once when this provider starts.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const login = useCallback(async (credentials: LoginRequest) => {
    const response = await loginRequest(credentials);
    const nextSession = { token: response.token, user: parseTokenClaims(response.token) };
    queryClient.clear();
    writeSession(nextSession);
    setSession(nextSession);
    setStatus('authenticated');
    return nextSession;
  }, [queryClient]);

  const value = useMemo<AuthContextValue>(() => ({ status, session, login, logout: purgeSession }), [status, session, login, purgeSession]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
