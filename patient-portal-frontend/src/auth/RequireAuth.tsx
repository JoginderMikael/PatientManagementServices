import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { Skeleton } from '../components/feedback/Skeleton';
import { useAuth } from './AuthProvider';

export function RequireAuth() {
  const { status } = useAuth();
  const location = useLocation();
  if (status === 'restoring') return <main className="centered-state"><Skeleton label="Restoring your secure session" lines={3} /></main>;
  if (status === 'anonymous') return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return <Outlet />;
}
