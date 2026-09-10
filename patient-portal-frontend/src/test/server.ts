import { setupServer } from 'msw/node';
import { authHandlers } from './handlers/authHandlers';
import { portalHandlers } from './handlers/portalHandlers';
import { staffHandlers } from './handlers/staffHandlers';

export const server = setupServer(...authHandlers, ...portalHandlers, ...staffHandlers);
