# Webhook Commander

A Minecraft 1.21.4 Fabric mod that opens an HTTP webhook server to receive and execute commands via JSON requests.

## Features

-   **HTTP Webhook Server**: Opens an HTTP server on a configurable port (default: 8080)
-   **Command Execution**: Executes Minecraft commands received via POST requests
-   **CORS Support**: Allows cross-origin requests for browser-based integrations
-   **Optional Authentication**: Secure your webhook with a Bearer token
-   **Health Check Endpoint**: Monitor server status at `/health`

## Installation

1. Build the mod:

    ```bash
    ./gradlew build
    ```

2. Copy the generated JAR from `build/libs/webhook-commander-1.0.0.jar` to your Minecraft server's `mods` folder

3. Make sure you have Fabric Loader and Fabric API installed for Minecraft 1.21.4

4. Start your Minecraft server

## Configuration

On first run, the mod creates a config file at `config/webhook_commander.json`:

```json
{
    "port": 8080,
    "authToken": ""
}
```

| Option      | Default | Description                                                                                         |
| ----------- | ------- | --------------------------------------------------------------------------------------------------- |
| `port`      | `8080`  | The port the webhook server listens on                                                              |
| `authToken` | `""`    | Optional authentication token. If set, requests must include `Authorization: Bearer <token>` header |

## Usage

### Execute Command Endpoint

**POST** `/execute`

Send a command to be executed in the Minecraft world.

#### Request

```json
{
    "command": "/say Hello from TikTok!"
}
```

The leading slash in the command is optional.

#### Success Response (200)

```json
{
    "success": true,
    "message": "Command executed successfully",
    "result": 1
}
```

#### Error Response (400/500)

```json
{
    "success": false,
    "message": "Error description"
}
```

### Health Check Endpoint

**GET** `/health`

Returns the server status.

```json
{
    "status": "ok",
    "mod": "webhook_commander"
}
```

## Examples

### Basic command (curl)

```bash
curl -X POST http://localhost:8080/execute \
  -H "Content-Type: application/json" \
  -d '{"command": "/say Hello World!"}'
```

### With authentication

```bash
curl -X POST http://localhost:8080/execute \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-secret-token" \
  -d '{"command": "/give @a diamond 1"}'
```

### Give items to all players

```bash
curl -X POST http://localhost:8080/execute \
  -H "Content-Type: application/json" \
  ## Webhook Commander

  A lightweight Fabric mod that exposes a local HTTP webhook to execute Minecraft commands. Designed for server-side integrations such as streaming platforms or external automation services.

  Project goal
  - Provide a small, configurable webhook endpoint that can safely accept command requests and run them on the server.

  Quick links
  - Build: `./gradlew build`
  - Mod JAR: `build/libs/` (see `build.gradle` for artifact name)

  Requirements
  - Java 21 or newer
  - Fabric Loader and Fabric API compatible with Minecraft 1.21.4

  Configuration
  - On first run the mod writes a JSON config under `config/` with at least a `port` and optional `authToken`.

  Installing the built artifact
  1. Build locally: `./gradlew clean build`
  2. Copy the produced JAR from `build/libs/` into your server's `mods/` folder
  3. Start the server with Fabric

  Continuous integration and release
  - This repository includes a GitHub Actions workflow that runs on tag pushes matching `v*` (for example `v1.0.0`). The workflow builds the project and publishes the produced JARs as a GitHub release asset.
  - To create a release from CI: push a tag, e.g. `git tag v1.0.0 && git push origin v1.0.0`.

  Usage (HTTP)
  - POST /execute — JSON body: `{ "command": "/say hello" }`
  - GET /health — returns a small JSON status object

  Security recommendations
  - Always set an `authToken` for production and require `Authorization: Bearer <token>` headers.
  - Do not expose the webhook port publicly without a reverse proxy and TLS.
  - Limit network exposure via firewall rules and use network-level protections where appropriate.

  Contributing
  - Open an issue or a pull request. Keep changes focused and include a short description of the problem the change solves.

  License
  - MIT

  For implementation details, see the source under `src/main/java` and the Gradle configuration in `build.gradle`.
```
