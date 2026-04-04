# FopConverterJava

AKT-to-PDF conversion service using Apache FOP. Deployed on K8s as `convert-fop.dev.amphora.ee` (dev) and `convert-fop.svc.amphora.ee` (prod).

## Build

```bash
./gradlew war  # outputs build/libs/FopConverterJava-1.0.0.war
docker build -t fop-converter .
```

## API

- `POST /conversionservice/api/conversion` — Convert AKT to PDF (JSON body with Base64 data)
- `GET /conversionservice/api/stillAlive` — Health check

## K8s

Manifests in AmphoraKubernetes repo: `workloads/fop-converter/`
Image: `ghcr.io/amphora-infohaldus/fop-converter:1.0.0`
NodePort: 30881
