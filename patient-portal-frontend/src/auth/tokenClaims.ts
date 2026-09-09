export const USER_ROLES = [
  'ADMIN', 'PATIENT', 'CLINICIAN', 'NURSE', 'REGISTRATION_STAFF', 'RECEPTIONIST',
  'BILLING_STAFF', 'PHARMACIST', 'LAB_STAFF', 'PRIVACY_OFFICER', 'AUDITOR', 'ANALYST',
] as const;

export type UserRole = typeof USER_ROLES[number];

export interface TokenClaims {
  subject: string;
  email: string;
  role: UserRole;
  patientId?: string;
  expiresAt: number;
}

export class InvalidTokenError extends Error {
  constructor() {
    super('The authentication response could not be verified.');
    this.name = 'InvalidTokenError';
  }
}

function decodeBase64Url(value: string): string {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
  return decodeURIComponent(Array.from(atob(padded), character =>
    `%${character.charCodeAt(0).toString(16).padStart(2, '0')}`).join(''));
}

export function parseTokenClaims(token: string): TokenClaims {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) throw new InvalidTokenError();
    const payload: unknown = JSON.parse(decodeBase64Url(parts[1]));
    if (!payload || typeof payload !== 'object') throw new InvalidTokenError();
    const claims = payload as Record<string, unknown>;
    const role = typeof claims.role === 'string' ? claims.role.toUpperCase() : '';
    if (typeof claims.sub !== 'string' || typeof claims.email !== 'string' ||
        typeof claims.exp !== 'number' || !USER_ROLES.includes(role as UserRole)) {
      throw new InvalidTokenError();
    }
    if (claims.exp * 1000 <= Date.now()) throw new InvalidTokenError();
    return {
      subject: claims.sub,
      email: claims.email,
      role: role as UserRole,
      patientId: typeof claims.patient_id === 'string' ? claims.patient_id : undefined,
      expiresAt: claims.exp * 1000,
    };
  } catch (error) {
    if (error instanceof InvalidTokenError) throw error;
    throw new InvalidTokenError();
  }
}
