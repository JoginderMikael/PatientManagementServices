import { setupServer } from 'msw/node';
import { authHandlers } from './handlers/authHandlers';
import { portalHandlers } from './handlers/portalHandlers';

export const server = setupServer(...authHandlers, ...portalHandlers);
