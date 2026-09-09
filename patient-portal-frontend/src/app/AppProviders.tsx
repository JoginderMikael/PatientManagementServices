import React from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { isApiError } from "../api/errors";
import { AuthProvider } from "../auth/AuthProvider";
import { AppErrorBoundary } from "../components/feedback/AppErrorBoundary";

export function shouldRetryQuery(
  failureCount: number,
  error: unknown,
): boolean {
  if (failureCount >= 2) return false;
  return isApiError(error) ? error.retryable : error instanceof TypeError;
}

export function createAppQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: shouldRetryQuery,
        retryDelay: (attempt) => Math.min(500 * 2 ** attempt, 2_000),
        staleTime: 15_000,
        gcTime: 5 * 60_000,
        refetchOnWindowFocus: false,
      },
      mutations: { retry: false },
    },
  });
}

const defaultQueryClient = createAppQueryClient();

export function AppProviders({
  children,
  queryClient = defaultQueryClient,
}: {
  children: React.ReactNode;
  queryClient?: QueryClient;
}) {
  return (
    <AppErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>{children}</AuthProvider>
      </QueryClientProvider>
    </AppErrorBoundary>
  );
}
