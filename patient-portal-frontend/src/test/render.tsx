import React from "react";
import { QueryClient } from "@tanstack/react-query";
import { render, RenderOptions } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AppProviders, createAppQueryClient } from "../app/AppProviders";

interface Options extends Omit<RenderOptions, "wrapper"> {
  route?: string;
  queryClient?: QueryClient;
}

export function renderWithProviders(
  ui: React.ReactElement,
  {
    route = "/",
    queryClient = createAppQueryClient(),
    ...options
  }: Options = {},
) {
  function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <MemoryRouter
        initialEntries={[route]}
        future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
      >
        <AppProviders queryClient={queryClient}>{children}</AppProviders>
      </MemoryRouter>
    );
  }
  return { queryClient, ...render(ui, { wrapper: Wrapper, ...options }) };
}
