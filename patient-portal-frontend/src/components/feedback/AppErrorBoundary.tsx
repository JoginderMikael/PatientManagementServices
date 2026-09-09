import React from "react";
import { ErrorState } from "./ErrorState";

interface State {
  hasError: boolean;
}

export class AppErrorBoundary extends React.Component<
  { children: React.ReactNode },
  State
> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError)
      return (
        <main className="centered-state">
          <ErrorState
            title="The application could not continue"
            message="Refresh the page. If the problem continues, contact support."
          />
        </main>
      );
    return this.props.children;
  }
}
