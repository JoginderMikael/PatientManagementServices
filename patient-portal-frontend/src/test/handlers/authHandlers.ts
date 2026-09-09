import { rest } from 'msw';
import { API_BASE_URL } from '../../api/client';
import { UserRole } from '../../auth/tokenClaims';
import { createSyntheticToken } from '../factories/sessionFactory';

const roleByEmail: Record<string, UserRole> = {
  'clinician@example.test': 'CLINICIAN', 'patient@example.test': 'PATIENT', 'billing@example.test': 'BILLING_STAFF',
};

export const authHandlers = [
  rest.post(`${API_BASE_URL}/auth/login`, async (request, response, context) => {
    const body = await request.json<{ email?: string; password?: string }>();
    const role = body.email ? roleByEmail[body.email] : undefined;
    if (!role || body.password !== 'synthetic-password') return response(context.status(401));
    return response(context.status(200), context.json({ token: createSyntheticToken(role, body.email) }));
  }),
  rest.get(`${API_BASE_URL}/auth/validate`, (request, response, context) =>
    request.headers.get('authorization')?.startsWith('Bearer ') ? response(context.status(200)) : response(context.status(401))),
  rest.get(`${API_BASE_URL}/test/unauthorized`, (_request, response, context) => response(context.status(401))),
  rest.get(`${API_BASE_URL}/test/forbidden`, (_request, response, context) => response(context.status(403))),
];
