# How To

## Prerequisites

- Java 17+ (Temurin 17 is configured in `~/.mavenrc`)
- Maven 3.9+ (wrapper included, no install needed)
- A Kanboard instance with API access
- A Kanboard API token

## Quick Start

```bash
cd /Users/marcus/Projects/Personal/KanbanApi
export KANBOARD_API_TOKEN=<your-token>
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080` with the MCP endpoint at `/mcp`.

## Configuration

All config lives in `src/main/resources/application.yml`. Override via environment variables:

| Env Var | Default | Description |
|---------|---------|-------------|
| `KANBOARD_URL` | `https://marcushowarth.com/jsonrpc.php` | Kanboard JSON-RPC endpoint |
| `KANBOARD_API_TOKEN` | *(none)* | API token for authentication |
| `KANBOARD_USERNAME` | `jsonrpc` | Username for Basic Auth header |

## Register with Claude Code

### Option 1: Project-level (via `.mcp.json`)

The included `.mcp.json` registers automatically when Claude Code is opened from the project directory. The server must already be running.

### Option 2: Manual registration

```bash
claude mcp add kanboard --transport http http://localhost:8080/mcp
```

## Test the MCP Endpoint

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "initialize",
    "id": 1,
    "params": {
      "protocolVersion": "2025-03-26",
      "capabilities": {},
      "clientInfo": {"name": "test", "version": "0.1"}
    }
  }'
```

## Build Only

```bash
./mvnw compile          # compile
./mvnw package          # compile + test + package
./mvnw clean package    # full clean build
```

## Maven / Java Notes

- Maven's JDK is set in `~/.mavenrc`, not `JAVA_HOME` in the shell
- The `~/.m2/settings.xml` has a Vision 360 Nexus mirror (`<mirrorOf>*</mirrorOf>`) — all dependency downloads go through `nexus.vision-development.co.uk`
- The Maven wrapper distribution URL is in `.mvn/wrapper/maven-wrapper.properties`

## Adding New Tools

1. Create a new `@Service` class in `com.kanbanapi.tools`
2. Add methods annotated with `@Tool(description = "...")`
3. Use `@ToolParam(description = "...")` on parameters
4. Inject `KanboardClient` and call `client.executePretty("kanboardMethod", params)`
5. Add the new service to `McpToolConfig.kanboardTools()` in the `toolObjects(...)` call
