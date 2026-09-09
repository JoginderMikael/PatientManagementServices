import { ApiError, ApiErrorPayload, defaultErrorMessage } from './errors';

export type ResponseType = 'auto' | 'json' | 'text' | 'empty';

export interface ApiRequestOptions<TBody = unknown>
  extends Omit<RequestInit, 'body' | 'headers'> {
  body?: TBody;
  token?: string;
  accept?: string;
  responseType?: ResponseType;
  headers?: Record<string, string>;
  idempotencyKey?: string;
  notifyOnUnauthorized?: boolean;
}

type UnauthorizedHandler = () => void;
let unauthorizedHandler: UnauthorizedHandler | null = null;

export const API_BASE_URL = (process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:4004').replace(/\/$/, '');

export function setUnauthorizedHandler(handler: UnauthorizedHandler | null): void {
  unauthorizedHandler = handler;
}

export function createCorrelationId(): string {
  const secureCrypto = globalThis.crypto;
  if (!secureCrypto) throw new Error('Secure random number generation is unavailable.');
  if (typeof secureCrypto.randomUUID === 'function') return secureCrypto.randomUUID();
  const bytes = new Uint8Array(16);
  secureCrypto.getRandomValues(bytes);
  bytes[6] = (bytes[6] & 0x0f) | 0x40;
  bytes[8] = (bytes[8] & 0x3f) | 0x80;
  const hex = Array.from(bytes, value => value.toString(16).padStart(2, '0')).join('');
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

export function createIdempotencyKey(): string {
  return createCorrelationId();
}

function isBodyInit(value: unknown): value is BodyInit {
  return typeof value === 'string' || value instanceof Blob || value instanceof FormData ||
    value instanceof URLSearchParams || value instanceof ArrayBuffer || ArrayBuffer.isView(value);
}

async function readError(response: Response): Promise<ApiErrorPayload> {
  const requestId = response.headers.get('x-request-id') ?? response.headers.get('x-correlation-id') ?? undefined;
  const contentType = response.headers.get('content-type') ?? '';
  try {
    if (contentType.includes('json')) {
      const payload: unknown = await response.json();
      if (payload && typeof payload === 'object') {
        const candidate = payload as Record<string, unknown>;
        return {
          code: typeof candidate.code === 'string' ? candidate.code : undefined,
          message: typeof candidate.message === 'string' ? candidate.message : undefined,
          fieldErrors: isFieldErrors(candidate.fieldErrors) ? candidate.fieldErrors : undefined,
          requestId: typeof candidate.requestId === 'string' ? candidate.requestId : requestId,
        };
      }
    }
  } catch {
    // Do not expose or log malformed backend bodies.
  }
  return { requestId };
}

function isFieldErrors(value: unknown): value is Record<string, string[]> {
  return Boolean(value) && typeof value === 'object' && Object.values(value as Record<string, unknown>)
    .every(messages => Array.isArray(messages) && messages.every(message => typeof message === 'string'));
}

async function decodeResponse<T>(response: Response, responseType: ResponseType): Promise<T> {
  if (responseType === 'empty' || response.status === 204 || response.headers.get('content-length') === '0') {
    return undefined as T;
  }
  if (responseType === 'text') return await response.text() as T;
  const contentType = response.headers.get('content-type') ?? '';
  if (responseType === 'json' || contentType.includes('json') || contentType.includes('+json')) {
    return await response.json() as T;
  }
  const text = await response.text();
  return (text.length ? text : undefined) as T;
}

export async function apiRequest<TResponse, TBody = unknown>(
  path: string,
  options: ApiRequestOptions<TBody> = {},
): Promise<TResponse> {
  const { body, token, accept = 'application/json', responseType = 'auto', idempotencyKey, notifyOnUnauthorized = true, ...requestOptions } = options;
  const headers = new Headers(options.headers);
  headers.set('Accept', accept);
  headers.set('X-Correlation-ID', createCorrelationId());
  if (token) headers.set('Authorization', `Bearer ${token}`);
  if (idempotencyKey) headers.set('Idempotency-Key', idempotencyKey);

  let requestBody: BodyInit | undefined;
  if (body !== undefined && body !== null) {
    if (isBodyInit(body)) requestBody = body;
    else {
      requestBody = JSON.stringify(body);
      headers.set('Content-Type', 'application/json');
    }
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { ...requestOptions, headers, body: requestBody });
  } catch {
    throw new ApiError({ status: 0, message: 'The service could not be reached.', retryable: true });
  }

  if (!response.ok) {
    const payload = await readError(response);
    if (response.status === 401 && notifyOnUnauthorized) unauthorizedHandler?.();
    throw new ApiError({
      status: response.status,
      message: payload.message ?? defaultErrorMessage(response.status),
      code: payload.code,
      fieldErrors: payload.fieldErrors,
      requestId: payload.requestId,
    });
  }

  return decodeResponse<TResponse>(response, responseType);
}
