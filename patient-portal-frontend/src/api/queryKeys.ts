export const queryKeys = {
  session: ['session'] as const,
  patient: (patientId: string) => ['patients', patientId] as const,
  patientResource: (patientId: string, resource: string) => ['patients', patientId, resource] as const,
  portalOverview: (patientId: string) => ['patients', patientId, 'portal-overview'] as const,
  staffQueue: (role: string, filters: Readonly<Record<string, string>>) => ['staff-queue', role, filters] as const,
};
