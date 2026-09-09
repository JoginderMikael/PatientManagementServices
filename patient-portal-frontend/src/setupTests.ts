// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';
import 'jest-axe/extend-expect';
import { clearSession } from './auth/session';
import { server } from './test/server';

Object.defineProperty(globalThis, 'crypto', {
  configurable: true,
  value: require('crypto').webcrypto,
});

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => {
  server.resetHandlers();
  clearSession();
});
afterAll(() => server.close());
