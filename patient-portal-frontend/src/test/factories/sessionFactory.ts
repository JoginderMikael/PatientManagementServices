import { AuthSession } from '../../auth/session';
import { UserRole } from '../../auth/tokenClaims';

function encode(value: unknown): string {
  return btoa(JSON.stringify(value)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
}

export function createSyntheticToken(role: UserRole = 'CLINICIAN', email = 'clinician@example.test'): string {
  const now = Math.floor(Date.now() / 1000);
  return `${encode({ alg: 'HS256', typ: 'JWT' })}.${encode({
    sub: '10000000-0000-4000-8000-000000000001', email, role, roles: [role], iat: now, exp: now + 3600,
  })}.synthetic-signature`;
}

export function createSyntheticSession(role: UserRole = 'CLINICIAN', email = 'clinician@example.test'): AuthSession {
  return { token: createSyntheticToken(role, email), user: { subject: '10000000-0000-4000-8000-000000000001', email, role, expiresAt: Date.now() + 3600_000 } };
}
