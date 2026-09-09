import { TokenClaims } from './tokenClaims';

export interface AuthSession {
  token: string;
  user: TokenClaims;
}

let inMemorySession: AuthSession | null = null;

export function readSession(): AuthSession | null {
  return inMemorySession;
}

export function writeSession(session: AuthSession): void {
  inMemorySession = session;
}

export function clearSession(): void {
  inMemorySession = null;
}
