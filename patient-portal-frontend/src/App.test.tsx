import React, { useState } from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { axe } from 'jest-axe';
import App from './App';
import { apiRequest, setUnauthorizedHandler } from './api/client';
import { AppRoutes } from './app/router';
import { useAuth } from './auth/AuthProvider';
import { writeSession } from './auth/session';
import { Tabs } from './components/data/Tabs';
import { Dialog } from './components/safety/Dialog';
import { createSyntheticSession } from './test/factories/sessionFactory';
import { renderWithProviders } from './test/render';

function setBrowserRoute(path: string) {
  window.history.pushState({}, '', path);
}

function signIn(email = 'clinician@example.test') {
  userEvent.type(screen.getByLabelText(/email address/i), email);
  userEvent.type(screen.getByLabelText(/^password/i), 'synthetic-password');
  userEvent.click(screen.getByRole('button', { name: /^sign in$/i }));
}

describe('application foundation', () => {
  test('valid login enters the role start page and filters staff navigation', async () => {
    setBrowserRoute('/login');
    render(<App />);
    signIn();
    expect(await screen.findByRole('heading', { name: 'Work queue' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Patients' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Billing' })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Compliance' })).not.toBeInTheDocument();
  });

  test('patient login enters the portal and never exposes staff navigation', async () => {
    setBrowserRoute('/login');
    render(<App />);
    signIn('patient@example.test');
    expect(await screen.findByRole('heading', { name: 'Overview' })).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Account summary' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Health records' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Work queue' })).not.toBeInTheDocument();
  });

  test('invalid login retains input and shows a neutral credential error', async () => {
    setBrowserRoute('/login');
    render(<App />);
    signIn('unknown@example.test');
    expect(await screen.findByText('The email or password is incorrect.')).toBeInTheDocument();
    expect(screen.getByLabelText(/email address/i)).toHaveValue('unknown@example.test');
    expect(screen.queryByText('Session ended')).not.toBeInTheDocument();
  });

  test('login validation links the error summary to required fields', async () => {
    setBrowserRoute('/login');
    render(<App />);
    userEvent.click(screen.getByRole('button', { name: /^sign in$/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Review 2 fields');
    expect(screen.getByRole('link', { name: 'Enter your email address.' })).toHaveAttribute('href', '#email');
    expect(screen.getByLabelText(/email address/i)).toHaveAttribute('aria-invalid', 'true');
  });

  test.each([320, 1440])('login form has no basic accessibility violations at %ipx', async (width) => {
    Object.defineProperty(window, 'innerWidth', { configurable: true, value: width });
    setBrowserRoute('/login');
    const { container } = render(<App />);
    await screen.findByRole('heading', { name: 'Account sign in' });
    expect(await axe(container)).toHaveNoViolations();
  });

  test('direct protected entry redirects an anonymous user to login', async () => {
    setBrowserRoute('/staff/work-queue');
    render(<App />);
    expect(await screen.findByRole('heading', { name: 'Account sign in' })).toBeInTheDocument();
  });

  test('role guard preserves an authenticated session on predicted 403', async () => {
    writeSession(createSyntheticSession('CLINICIAN'));
    setBrowserRoute('/staff/billing');
    render(<App />);
    expect(await screen.findByRole('heading', { name: /do not have access/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /start page/i })).toBeInTheDocument();
  });

  test('backend 401 invokes the session-expiry boundary', async () => {
    let expired = false;
    setUnauthorizedHandler(() => { expired = true; });
    await expect(apiRequest('/test/unauthorized')).rejects.toMatchObject({ status: 401 });
    expect(expired).toBe(true);
    setUnauthorizedHandler(null);
  });

  test('backend 401 clears session and cached server state', async () => {
    writeSession(createSyntheticSession('CLINICIAN'));
    function Probe() {
      const { session } = useAuth();
      return <button onClick={() => apiRequest('/test/unauthorized', { token: session?.token }).catch(() => undefined)}>Trigger 401</button>;
    }
    const { queryClient } = renderWithProviders(<Probe />);
    queryClient.setQueryData(['patients', 'synthetic'], { sensitive: true });
    userEvent.click(await screen.findByRole('button', { name: 'Trigger 401' }));
    await waitFor(() => expect(queryClient.getQueryData(['patients', 'synthetic'])).toBeUndefined());
  });

  test('backend 403 preserves the authenticated session and cache', async () => {
    writeSession(createSyntheticSession('CLINICIAN'));
    function Probe() {
      const { session } = useAuth();
      return <><span>{session?.user.email}</span><button onClick={() => apiRequest('/test/forbidden', { token: session?.token }).catch(() => undefined)}>Trigger 403</button></>;
    }
    const { queryClient } = renderWithProviders(<Probe />);
    queryClient.setQueryData(['patients', 'synthetic'], { sensitive: true });
    userEvent.click(await screen.findByRole('button', { name: 'Trigger 403' }));
    await waitFor(() => expect(screen.getByText('clinician@example.test')).toBeInTheDocument());
    expect(queryClient.getQueryData(['patients', 'synthetic'])).toEqual({ sensitive: true });
  });

  test('manual logout clears the in-memory session and query cache', async () => {
    writeSession(createSyntheticSession('CLINICIAN'));
    function Probe() {
      const { session, logout } = useAuth();
      return <><span>{session?.user.email ?? 'anonymous'}</span><button onClick={() => logout()}>Sign out</button></>;
    }
    const { queryClient } = renderWithProviders(<Probe />);
    queryClient.setQueryData(['patients', 'synthetic'], { sensitive: true });
    expect(await screen.findByText('clinician@example.test')).toBeInTheDocument();
    userEvent.click(screen.getByRole('button', { name: 'Sign out' }));
    await waitFor(() => expect(screen.getByText('anonymous')).toBeInTheDocument());
    expect(queryClient.getQueryData(['patients', 'synthetic'])).toBeUndefined();
  });

  test.each([[320, '/patient/overview', 'PATIENT'], [1440, '/staff/work-queue', 'CLINICIAN']] as const)('has no basic accessibility violations at %ipx', async (width, path, role) => {
    Object.defineProperty(window, 'innerWidth', { configurable: true, value: width });
    writeSession(createSyntheticSession(role));
    const { container } = renderWithProviders(<AppRoutes />, { route: path });
    await screen.findByRole('heading', { name: path.startsWith('/staff') ? 'Work queue' : 'Overview' });
    if (path.startsWith('/patient')) await screen.findByRole('heading', { name: 'Account summary' });
    if (path.startsWith('/staff')) await screen.findByText('Review synthetic discharge plan');
    if (width === 320) {
      const toggle = screen.getByRole('button', { name: /toggle primary navigation/i });
      toggle.focus();
      userEvent.keyboard('{Enter}');
      expect(toggle).toHaveAttribute('aria-expanded', 'true');
    }
    expect(await axe(container)).toHaveNoViolations();
  });
});

describe('accessible primitives', () => {
  test('tabs support arrow-key navigation', () => {
    render(<Tabs label="Example sections" items={[{ id: 'one', label: 'One', panel: 'First' }, { id: 'two', label: 'Two', panel: 'Second' }]} />);
    const first = screen.getByRole('tab', { name: 'One' });
    first.focus();
    userEvent.keyboard('{arrowright}');
    expect(screen.getByRole('tab', { name: 'Two' })).toHaveFocus();
    expect(screen.getByText('Second')).toBeInTheDocument();
  });

  test('dialog closes with Escape and returns focus', () => {
    function Harness() {
      const [open, setOpen] = useState(false);
      return <><button onClick={() => setOpen(true)}>Open review</button><Dialog open={open} title="Review action" description="Confirm the selected resource." confirmLabel="Confirm" onClose={() => setOpen(false)} onConfirm={() => setOpen(false)} /></>;
    }
    render(<Harness />);
    const trigger = screen.getByRole('button', { name: 'Open review' });
    userEvent.click(trigger);
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    userEvent.keyboard('{Escape}');
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(trigger).toHaveFocus();
  });
});
