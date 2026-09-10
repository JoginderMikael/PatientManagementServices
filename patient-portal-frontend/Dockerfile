# syntax=docker/dockerfile:1
FROM node:22-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN --mount=type=cache,id=patient-management-npm,target=/root/.npm \
    npm install --global npm@11.6.2 \
    && npm ci --no-audit --no-fund

COPY public ./public
COPY src ./src
COPY tsconfig.json ./

# An empty value makes browser API requests same-origin. The runtime server then
# proxies /auth and /api to the gateway over the Compose network.
ARG REACT_APP_API_BASE_URL=
ENV REACT_APP_API_BASE_URL=${REACT_APP_API_BASE_URL}

RUN npm run build

FROM node:22-alpine AS runtime

WORKDIR /app

ENV NODE_ENV=production \
    PORT=8080 \
    API_GATEWAY_URL=http://api-gateway:4004

COPY container/server.mjs ./server.mjs
COPY --from=builder --chown=node:node /app/build ./build

EXPOSE 8080

USER node

CMD ["node", "server.mjs"]
