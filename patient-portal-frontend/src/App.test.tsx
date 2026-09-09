import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders patient dashboard', () => {
  render(<App />);
  const dashboardElement = screen.getByText(/Patient Dashboard/i);
  expect(dashboardElement).toBeInTheDocument();
});
