# FopConverterJava

AKT-to-PDF conversion service using Apache FOP. Runs on K8s (dev, prod, hetzner-dr) and standalone Docker.

## Build

```bash
./gradlew war  # outputs build/libs/conversionservice.war
docker build -t fop-converter .
```

## API

- `POST /conversionservice/api/conversion` — Convert AKT to PDF (JSON body with Base64 data)
- `GET /conversionservice/api/stillAlive` — Health check

## Telemetry

OpenTelemetry Java agent is baked into the Docker image. Configure at deploy time via environment variables:

```
OTEL_EXPORTER_OTLP_ENDPOINT=https://otel.svc.amphora.ee
OTEL_EXPORTER_OTLP_HEADERS=Authorization=Bearer <token>
```

K8s deployments use init containers to download the agent and set env vars per environment. See `AmphoraKubernetes/workloads/convert-fop/`.

## Deployment

Image: `ghcr.io/amphora-infohaldus/fop-converter`
K8s manifests: `AmphoraKubernetes/workloads/convert-fop/`

| Environment | URL | NodePort |
|---|---|---|
| Dev | `convert-fop.dev.amphora.ee` | 30881 |
| Prod | `convert-fop.svc.amphora.ee` | 30881 |
| Hetzner DR | `convert-fop.svc-hcloud.amphora.ee` | — (Ingress) |

## Releasing

```bash
git tag v1.2.0
git push origin v1.2.0
```

CI builds the Docker image, pushes to GHCR with the version tag + `:latest`, and creates a GitHub Release. Never move existing tags — always create a new version.
