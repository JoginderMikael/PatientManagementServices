export interface ApiErrorPayload {
  code?: string;
  message?: string;
  fieldErrors?: Record<string, string[]>;
  requestId?: string;
}

export class ApiError extends Error {
  readonly status: number;
  readonly code?: string;
  readonly fieldErrors?: Record<string, string[]>;
  readonly requestId?: string;
  readonly retryable: boolean;

  constructor(options: {
    status: number;
    message: string;
    code?: string;
    fieldErrors?: Record<string, string[]>;
    requestId?: string;
    retryable?: boolean;
  }) {
    super(options.message);
    this.name = 'ApiError';
    this.status = options.status;
    this.code = options.code;
    this.fieldErrors = options.fieldErrors;
    this.requestId = options.requestId;
    this.retryable = options.retryable ?? (options.status === 0 || options.status === 503);
  }
}

export function defaultErrorMessage(status: number): string {
  switch (status) {
    case 400: return 'The request could not be accepted. Review the information and try again.';
    case 401: return 'Your session is no longer valid. Sign in again.';
    case 403: return 'You do not have access to perform this action.';
    case 404: return 'The requested information could not be found.';
    case 409: return 'This information changed. Review the current state before continuing.';
    case 410: return 'This information is no longer available.';
    case 422: return 'Some information needs your attention.';
    case 503: return 'This service is temporarily unavailable. Try again shortly.';
    default: return 'Something went wrong. Try again or contact support.';
  }
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError;
}
