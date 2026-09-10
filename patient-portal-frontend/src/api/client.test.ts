import { rest } from "msw";
import { apiRequest, API_BASE_URL, createIdempotencyKey } from "./client";
import { shouldRetryQuery } from "../app/AppProviders";
import { server } from "../test/server";
import { ApiError } from "./errors";

describe("gateway API client", () => {
  test("adds a correlation ID and decodes JSON", async () => {
    let correlationId = "";
    server.use(
      rest.get(`${API_BASE_URL}/test/json`, (request, response, context) => {
        correlationId = request.headers.get("x-correlation-id") ?? "";
        return response(context.json({ state: "ready" }));
      }),
    );
    await expect(apiRequest<{ state: string }>("/test/json")).resolves.toEqual({
      state: "ready",
    });
    expect(correlationId).toMatch(/^[0-9a-f-]{36}$/);
  });

  test("decodes text and empty responses intentionally", async () => {
    server.use(
      rest.get(`${API_BASE_URL}/test/text`, (_request, response, context) =>
        response(
          context.set("Content-Type", "text/plain"),
          context.body("synthetic release"),
        ),
      ),
      rest.post(`${API_BASE_URL}/test/empty`, (_request, response, context) =>
        response(context.status(204)),
      ),
    );
    await expect(
      apiRequest<string>("/test/text", { responseType: "text" }),
    ).resolves.toBe("synthetic release");
    await expect(
      apiRequest<void>("/test/empty", {
        method: "POST",
        responseType: "empty",
      }),
    ).resolves.toBeUndefined();
  });

  test("normalizes validation errors and request IDs", async () => {
    server.use(
      rest.post(
        `${API_BASE_URL}/test/validation`,
        (_request, response, context) =>
          response(
            context.status(422),
            context.set("X-Request-ID", "REQ-SYNTHETIC-12"),
            context.json({
              code: "VALIDATION",
              message: "Review the submitted fields.",
              fieldErrors: { email: ["Email is invalid."] },
            }),
          ),
      ),
    );
    await expect(
      apiRequest("/test/validation", {
        method: "POST",
        body: { email: "invalid" },
      }),
    ).rejects.toMatchObject({
      status: 422,
      code: "VALIDATION",
      requestId: "REQ-SYNTHETIC-12",
      retryable: false,
      fieldErrors: { email: ["Email is invalid."] },
    });
  });

  test("retries only transient read failures within the configured bound", () => {
    expect(
      shouldRetryQuery(
        0,
        new ApiError({ status: 503, message: "Unavailable" }),
      ),
    ).toBe(true);
    expect(
      shouldRetryQuery(
        0,
        new ApiError({ status: 502, message: "Downstream unavailable" }),
      ),
    ).toBe(true);
    expect(
      shouldRetryQuery(0, new ApiError({ status: 409, message: "Conflict" })),
    ).toBe(false);
    expect(shouldRetryQuery(0, new TypeError("network"))).toBe(true);
    expect(shouldRetryQuery(2, new TypeError("network"))).toBe(false);
  });

  test("creates cryptographically shaped idempotency keys", () => {
    expect(createIdempotencyKey()).toMatch(/^[0-9a-f-]{36}$/);
    expect(createIdempotencyKey()).not.toBe(createIdempotencyKey());
  });
});
