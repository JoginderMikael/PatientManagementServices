import { rest } from 'msw';
import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { axe } from 'jest-axe';
import { API_BASE_URL } from '../../api/client';
import { AppRoutes } from '../../app/router';
import { writeSession } from '../../auth/session';
import { createSyntheticSession } from '../../test/factories/sessionFactory';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { syntheticOverview } from '../../test/handlers/portalHandlers';

function renderPatientRoute(route: string) {
  writeSession(createSyntheticSession('PATIENT', 'patient@example.test'));
  return renderWithProviders(<AppRoutes />, { route });
}

describe('minimal patient portal', () => {
  test('loads the overview and distinguishes an empty patient account', async () => {
    server.use(rest.get(`${API_BASE_URL}/api/portal/patients/:patientId/overview`, (_request, response, context) => response(context.json({ ...syntheticOverview, appointments: [], recordRequests: [], payments: [] }))));
    renderPatientRoute('/patient/overview');
    expect(await screen.findByRole('heading', { name: 'Account summary' })).toBeInTheDocument();
    expect(screen.getByText('No appointment requests yet.')).toBeInTheDocument();
    expect(screen.getByText('No record requests yet.')).toBeInTheDocument();
    expect(screen.getByText('No payment activity yet.')).toBeInTheDocument();
  });

  test('submits and deliberately cancels an unresolved appointment request', async () => {
    renderPatientRoute('/patient/appointments');
    await screen.findByText('Synthetic wellness visit');
    userEvent.selectOptions(screen.getByLabelText(/Preferred specialty/i), 'Cardiology');
    userEvent.type(screen.getByLabelText(/Reason for visit/i), 'Synthetic follow-up request');
    userEvent.click(screen.getByRole('button', { name: 'Submit request' }));
    expect(await screen.findByText('Request submitted')).toBeInTheDocument();
    expect(screen.getByText('Synthetic follow-up request')).toBeInTheDocument();

    userEvent.click(screen.getAllByRole('button', { name: 'Cancel request' })[0]);
    const dialog = screen.getByRole('dialog', { name: 'Cancel appointment request' });
    expect(dialog).toBeInTheDocument();
    userEvent.click(within(dialog).getByRole('button', { name: 'Cancel request' }));
    await waitFor(() => expect(screen.getByText('Resolved')).toBeInTheDocument());
  });

  test('requests records and downloads a fulfilled release without rendering its content', async () => {
    const createObjectURL = jest.fn(() => 'blob:synthetic-release');
    const revokeObjectURL = jest.fn();
    Object.defineProperty(URL, 'createObjectURL', { configurable: true, value: createObjectURL });
    Object.defineProperty(URL, 'revokeObjectURL', { configurable: true, value: revokeObjectURL });
    jest.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined);
    renderPatientRoute('/patient/records');
    const download = await screen.findByRole('button', { name: 'Download records.txt' });
    userEvent.click(download);
    await waitFor(() => expect(createObjectURL).toHaveBeenCalledTimes(1));
    expect(screen.queryByText('Synthetic record release')).not.toBeInTheDocument();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:synthetic-release');

    userEvent.selectOptions(screen.getByLabelText(/Record type/i), 'CLINICAL_SUMMARY');
    userEvent.click(screen.getByRole('checkbox'));
    userEvent.click(screen.getByRole('button', { name: 'Submit request' }));
    expect(await screen.findByText('Records requested')).toBeInTheDocument();
    expect(screen.getByText('CLINICAL SUMMARY')).toBeInTheDocument();
  });

  test('reuses the payment idempotency key for a deliberate retry of unchanged details', async () => {
    const keys: string[] = [];
    server.use(rest.post(`${API_BASE_URL}/api/portal/payments`, async (request, response, context) => {
      keys.push(request.headers.get('idempotency-key') ?? '');
      const body = await request.json<Record<string, unknown>>();
      if (keys.length === 1) return response(context.status(503));
      return response(context.status(201), context.json({ id: '40000000-0000-4000-8000-000000000099', ...body, status: 'SUBMITTED', createdAt: '2026-09-09T08:00:00Z' }));
    }));
    renderPatientRoute('/patient/billing');
    await screen.findByText('Invoice details required');
    userEvent.type(screen.getByLabelText(/Invoice ID/i), '50000000-0000-4000-8000-000000000099');
    userEvent.type(screen.getByLabelText(/Amount \(KES\)/i), '850.00');
    userEvent.click(screen.getByRole('button', { name: 'Submit payment' }));
    expect(await screen.findByText('Request not completed')).toBeInTheDocument();
    userEvent.click(screen.getByRole('button', { name: 'Submit payment' }));
    expect(await screen.findByText('Payment submitted')).toBeInTheDocument();
    expect(keys).toHaveLength(2);
    expect(keys[0]).toBe(keys[1]);
  });

  test('keeps owner denial and not-ready record download distinct', async () => {
    server.use(rest.get(`${API_BASE_URL}/api/portal/patients/:patientId/overview`, (_request, response, context) => response(context.status(403))));
    const denied = renderPatientRoute('/patient/overview');
    expect(await screen.findByRole('heading', { name: 'Patient access not available' })).toBeInTheDocument();
    denied.unmount();

    server.resetHandlers();
    server.use(rest.get(`${API_BASE_URL}/api/portal/record-requests/:id/download`, (_request, response, context) => response(context.status(409))));
    renderPatientRoute('/patient/records');
    userEvent.click(await screen.findByRole('button', { name: 'Download records.txt' }));
    expect(await screen.findByText('Current state needs review')).toBeInTheDocument();
    expect(screen.getByText(/release is not ready yet/i)).toBeInTheDocument();
  });

  test('shows a recoverable unavailable state after bounded transient retries', async () => {
    let attempts = 0;
    server.use(rest.get(`${API_BASE_URL}/api/portal/patients/:patientId/overview`, (_request, response, context) => {
      attempts += 1;
      return response(context.status(503));
    }));
    renderPatientRoute('/patient/overview');
    expect(await screen.findByRole('heading', { name: 'Portal information unavailable' }, { timeout: 5000 })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument();
    expect(attempts).toBe(3);
  });

  test.each([
    ['/patient/overview', 'Account summary'],
    ['/patient/appointments', 'Request an appointment'],
    ['/patient/records', 'Request health records'],
    ['/patient/billing', 'Make a payment'],
    ['/patient/access', 'Portal profile'],
  ])('%s has no basic mobile accessibility violations', async (route, readyHeading) => {
    Object.defineProperty(window, 'innerWidth', { configurable: true, value: 320 });
    const { container } = renderPatientRoute(route);
    await screen.findByRole('heading', { name: readyHeading });
    expect(await axe(container)).toHaveNoViolations();
  });
});
