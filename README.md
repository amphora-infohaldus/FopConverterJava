# FopConverterJava

AKT-to-PDF document conversion service using [Apache FOP](https://xmlgraphics.apache.org/fop/). Accepts AKT (Estonian legal document XML format) files with embedded XSL-FO stylesheets and converts them to PDF.

## Requirements

- Java 11+
- Gradle (wrapper included)

## Build

```bash
./gradlew war
```

Output: `build/libs/conversionservice.war`

### Docker

```bash
docker build -t fop-converter .
docker run -p 8080:8080 fop-converter
```

## API

Base path: `/conversionservice/api`

### Convert document

```
POST /conversionservice/api/conversion
Content-Type: application/json
```

Request body:

```json
{
  "Data": "<base64-encoded AKT XML>",
  "From": 35,
  "To": 4,
  "FromExtension": "akt",
  "ToExtension": "pdf"
}
```

`From` and `To` are 1-based indices into the `FileType` enum. AKT = 35, PDF = 4.

Response (200):

```json
{
  "File": "<base64-encoded PDF>",
  "Message": ""
}
```

### Health check

```
GET /conversionservice/api/stillAlive
```

Returns `Elvis is still alive!` with HTTP 200.

## Deployment

Docker image: `ghcr.io/amphora-infohaldus/fop-converter`

Kubernetes manifests are in the [AmphoraKubernetes](https://github.com/amphora-infohaldus/AmphoraKubernetes) repo under `workloads/fop-converter/`.

| Environment | URL |
|-------------|-----|
| Dev | `convert-fop.dev.amphora.ee` |
| Production | `convert-fop.svc.amphora.ee` |

NodePort: 30881

## Releasing

Push a version tag to create a release:

```bash
git tag v1.1.0
git push origin v1.1.0
```

This triggers the CI pipeline which builds the Docker image, pushes it to GHCR with the version tag, and creates a GitHub Release with auto-generated release notes.
