import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthProvider';
import { UserRole } from './tokenClaims';

export function RequireRole({ allow, children }: { allow: readonly UserRole[]; children?: React.ReactNode }) {
  const { session } = useAuth();
  if (!session || !allow.includes(session.user.role)) return <Navigate to="/forbidden" replace />;
  return children ? <>{children}</> : <Outlet />;
}
