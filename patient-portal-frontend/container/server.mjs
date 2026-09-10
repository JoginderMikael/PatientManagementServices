import { createReadStream, promises as fs } from "node:fs";
import http from "node:http";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = process.env.BUILD_ROOT
  ? path.resolve(process.env.BUILD_ROOT)
  : path.join(path.dirname(fileURLToPath(import.meta.url)), "build");
const port = Number.parseInt(process.env.PORT ?? "8080", 10);
const gateway = new URL(
  process.env.API_GATEWAY_URL ?? "http://api-gateway:4004",
);

const securityHeaders = {
  "Cross-Origin-Opener-Policy": "same-origin",
  "Cross-Origin-Resource-Policy": "same-origin",
  "Content-Security-Policy":
    "default-src 'self'; base-uri 'self'; connect-src 'self'; font-src 'self' data:; form-action 'self'; frame-ancestors 'none'; img-src 'self' data:; object-src 'none'; script-src 'self'; style-src 'self'",
  "Permissions-Policy": "camera=(), geolocation=(), microphone=()",
  "Referrer-Policy": "no-referrer",
  "X-Content-Type-Options": "nosniff",
  "X-Frame-Options": "DENY",
};

const contentTypes = new Map([
  [".css", "text/css; charset=utf-8"],
  [".html", "text/html; charset=utf-8"],
  [".ico", "image/x-icon"],
  [".js", "text/javascript; charset=utf-8"],
  [".json", "application/json; charset=utf-8"],
  [".map", "application/json; charset=utf-8"],
  [".png", "image/png"],
  [".svg", "image/svg+xml"],
  [".txt", "text/plain; charset=utf-8"],
  [".webmanifest", "application/manifest+json"],
  [".woff", "font/woff"],
  [".woff2", "font/woff2"],
]);

function send(res, status, headers, body) {
  res.writeHead(status, { ...securityHeaders, ...headers });
  res.end(body);
}

function proxy(req, res) {
  const headers = { ...req.headers, host: gateway.host };
  delete headers.connection;

  const upstream = http.request(
    {
      protocol: gateway.protocol,
      hostname: gateway.hostname,
      port: gateway.port,
      method: req.method,
      path: req.url,
      headers,
    },
    (upstreamResponse) => {
      res.writeHead(upstreamResponse.statusCode ?? 502, {
        ...upstreamResponse.headers,
        ...securityHeaders,
      });
      upstreamResponse.pipe(res);
    },
  );

  upstream.on("error", () => {
    if (!res.headersSent) {
      send(
        res,
        502,
        { "Content-Type": "application/json; charset=utf-8" },
        JSON.stringify({ message: "API gateway unavailable" }),
      );
    } else {
      res.destroy();
    }
  });
  req.pipe(upstream);
}

async function serveFile(req, res, filePath, cacheControl) {
  try {
    const stats = await fs.stat(filePath);
    if (!stats.isFile()) return false;

    const headers = {
      "Cache-Control": cacheControl,
      "Content-Length": stats.size,
      "Content-Type":
        contentTypes.get(path.extname(filePath)) ?? "application/octet-stream",
    };
    res.writeHead(200, { ...securityHeaders, ...headers });
    if (req.method === "HEAD") res.end();
    else createReadStream(filePath).pipe(res);
    return true;
  } catch (error) {
    if (error?.code === "ENOENT" || error?.code === "ENOTDIR") return false;
    throw error;
  }
}

const server = http.createServer(async (req, res) => {
  try {
    const pathname = new URL(req.url ?? "/", "http://frontend.local").pathname;

    if (pathname === "/healthz") {
      send(
        res,
        200,
        {
          "Cache-Control": "no-store",
          "Content-Type": "text/plain; charset=utf-8",
        },
        "healthy\n",
      );
      return;
    }

    if (pathname.startsWith("/api/") || pathname.startsWith("/auth/")) {
      proxy(req, res);
      return;
    }

    if (req.method !== "GET" && req.method !== "HEAD") {
      send(
        res,
        405,
        { Allow: "GET, HEAD", "Content-Type": "text/plain; charset=utf-8" },
        "Method not allowed\n",
      );
      return;
    }

    const decodedPath = decodeURIComponent(pathname);
    if (path.extname(decodedPath).toLowerCase() === ".map") {
      send(
        res,
        404,
        {
          "Cache-Control": "no-store",
          "Content-Type": "text/plain; charset=utf-8",
        },
        "Not found\n",
      );
      return;
    }
    const requested = path.resolve(root, `.${decodedPath}`);
    const insideRoot =
      requested === root || requested.startsWith(`${root}${path.sep}`);
    if (insideRoot && path.extname(requested)) {
      const immutable = /\.[a-f0-9]{8,}\./i.test(path.basename(requested));
      if (
        await serveFile(
          req,
          res,
          requested,
          immutable
            ? "public, max-age=31536000, immutable"
            : "no-cache",
        )
      )
        return;
      send(
        res,
        404,
        {
          "Cache-Control": "no-store",
          "Content-Type": "text/plain; charset=utf-8",
        },
        "Not found\n",
      );
      return;
    }

    if (!(await serveFile(req, res, path.join(root, "index.html"), "no-store"))) {
      send(
        res,
        503,
        {
          "Cache-Control": "no-store",
          "Content-Type": "text/plain; charset=utf-8",
        },
        "Frontend build unavailable\n",
      );
    }
  } catch {
    send(
      res,
      500,
      { "Content-Type": "text/plain; charset=utf-8" },
      "Internal server error\n",
    );
  }
});

server.listen(port, "0.0.0.0", () => {
  console.log(`Patient portal frontend listening on port ${port}`);
});

function shutdown() {
  server.close(() => process.exit(0));
  setTimeout(() => process.exit(1), 10_000).unref();
}

process.on("SIGINT", shutdown);
process.on("SIGTERM", shutdown);
