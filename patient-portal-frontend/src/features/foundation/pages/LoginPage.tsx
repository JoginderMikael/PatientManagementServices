import { useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { isApiError } from '../../../api/errors';
import { startRouteFor } from '../../../app/routePolicy';
import { useAuth } from '../../../auth/AuthProvider';
import { Alert } from '../../../components/feedback/Alert';
import { Field, TextField, ValidationSummary } from '../../../components/forms/FormControls';

const loginSchema = z.object({
  email: z.string().trim().min(1, 'Enter your email address.').email('Enter a valid email address.'),
  password: z.string().min(1, 'Enter your password.').min(8, 'Password must be at least 8 characters.'),
});

type LoginValues = z.infer<typeof loginSchema>;

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { register, handleSubmit, setError, formState: { errors, isSubmitting } } = useForm<LoginValues>({ resolver: zodResolver(loginSchema), defaultValues: { email: '', password: '' } });
  const expired = (location.state as { reason?: string } | null)?.reason === 'expired';
  const summary = useMemo(() => Object.entries(errors).map(([fieldId, error]) => ({ fieldId, message: error?.message ?? 'Review this field.' })), [errors]);

  async function onSubmit(values: LoginValues) {
    try {
      const session = await login(values);
      navigate(startRouteFor(session.user.role), { replace: true });
    } catch (error) {
      const message = isApiError(error) && error.status === 401 ? 'The email or password is incorrect.' :
        isApiError(error) ? error.message : 'Sign in could not be completed. Try again.';
      setError('root', { message });
    }
  }

  return <main className="login-page" id="main-content">
    <section className="login-context" aria-label="Northstar Health">
      <div className="brand brand--large"><span className="brand-mark" aria-hidden="true">N+</span><span><strong>Northstar Health</strong><small>Patient management</small></span></div>
      <div><p className="eyebrow">Secure care workspace</p><h1>Sign in to continue</h1><p>Access the patient portal or the clinical tools assigned to your role.</p></div>
      <small>Authorized use only · Activity is audited · UAT uses synthetic information</small>
    </section>
    <section className="login-panel">
      <form className="login-card" onSubmit={handleSubmit(onSubmit)} noValidate>
        <h2>Account sign in</h2><p>Use the account issued by your facility.</p>
        {expired && <Alert title="Session ended" tone="warning">Sign in again to continue.</Alert>}
        {errors.root?.message && <Alert title="Sign in failed" tone="danger">{errors.root.message}</Alert>}
        <ValidationSummary errors={summary.filter(error => error.fieldId !== 'root')} />
        <Field id="email" label="Email address" required error={errors.email?.message}>
          <TextField type="email" autoComplete="username" {...register('email')} />
        </Field>
        <Field id="password" label="Password" required error={errors.password?.message} help="At least 8 characters.">
          <TextField type="password" autoComplete="current-password" {...register('password')} />
        </Field>
        <button className="button button--primary button--full" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Signing in…' : 'Sign in'}</button>
        <p className="privacy-copy">Do not enter production credentials or real patient information in UAT.</p>
      </form>
    </section>
  </main>;
}
