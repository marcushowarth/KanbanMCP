# KanbanApi — Kanboard MCP Server

A Spring Boot MCP (Model Context Protocol) server that bridges Claude to a [Kanboard](https://kanboard.org) instance. Lets Claude read boards, manage tasks, add comments, and move cards — from Claude Desktop or Claude Code.

## Stack

- **Java 17**, Spring Boot 4.0.5, Spring AI 2.0.0-M3
- **MCP transport:** Streamable HTTP (MCP protocol 2025-11-25)
- **MCP endpoint:** `/mcp`
- **Health endpoint:** `/actuator/health`

## Tools (17)

| Class | Tool | What it does |
|-------|------|--------------|
| ProjectTools | `getAllProjects` | List all accessible projects |
| ProjectTools | `getProjectById` | Get project by ID |
| BoardTools | `getBoard` | Full board with columns and swimlanes |
| BoardTools | `getColumns` | Column IDs, names, positions |
| BoardTools | `moveTaskPosition` | Move task between columns |
| TaskTools | `searchTasks` | Search using Kanboard query syntax |
| TaskTools | `getTask` | Full task detail |
| TaskTools | `createTask` | Create task (title + projectId required) |
| TaskTools | `updateTask` | Update any task fields |
| TaskTools | `closeTask` | Mark task as done |
| TaskTools | `openTask` | Reopen a closed task |
| CommentTools | `getAllComments` | List comments on a task |
| CommentTools | `createComment` | Add a comment |
| SubtaskTools | `getAllSubtasks` | List subtasks (checklist items) on a task |
| SubtaskTools | `createSubtask` | Add a subtask to a task |
| SubtaskTools | `updateSubtask` | Update subtask title or status (0=todo, 1=in progress, 2=done) |
| SubtaskTools | `removeSubtask` | Delete a subtask |

## Configuration

| Env var | Default | Description |
|---------|---------|-------------|
| `KANBOARD_URL` | `https://kanban.howarth.eu/jsonrpc.php` | JSON-RPC endpoint |
| `KANBOARD_API_TOKEN` | *(required)* | API token from Kanboard profile |
| `KANBOARD_USERNAME` | `claude` | Basic auth username |

Get your API token: Kanboard → Profile → API → Personal token.

## Use with Claude Desktop

Add to `claude_desktop_config.json`:

**Remote (hosted):**
```json
{
  "mcpServers": {
    "kanban": {
      "command": "npx",
      "args": [
        "mcp-remote",
        "https://your-mcp-host/mcp"
      ]
    }
  }
}
```

**Local (running on your machine):**
```json
{
  "mcpServers": {
    "kanban": {
      "command": "npx",
      "args": [
        "mcp-remote",
        "http://localhost:8080/mcp"
      ]
    }
  }
}
```

Config file locations:
- **macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

Restart Claude Desktop after editing. The MCP server will appear in the tools panel.

## Use with Claude Code

A `.mcp.json` is included in this repo. Claude Code will pick it up automatically when run from this directory, pointing at `http://localhost:8080/mcp`.

## Run locally

```bash
export KANBOARD_API_TOKEN=your_token
./mvnw spring-boot:run
```

Server starts on port 8080. Test it:
```bash
curl http://localhost:8080/actuator/health
```

## Run with Docker

```bash
docker build -t kanban-api .
docker run -p 8080:8080 -e KANBOARD_API_TOKEN=your_token kanban-api
```

## Deploy to AWS (CI/CD)

This repo includes a GitHub Actions workflow (`.github/workflows/deploy.yml`) that runs on every push to `main`:

```
push to main
  → run tests
  → build Docker image
  → push to ECR
  → SSH deploy to EC2
```

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `AWS_ACCESS_KEY_ID` | IAM user with ECR push permissions |
| `AWS_SECRET_ACCESS_KEY` | Corresponding secret key |
| `EC2_HOST` | Public IP of your EC2 instance |
| `EC2_SSH_KEY` | Contents of your `.pem` key file |
| `KANBOARD_API_TOKEN` | Passed to the container at runtime |

### AWS setup summary

- **ECR** — container registry in your region
- **EC2** — t3.micro, Amazon Linux 2023, Docker installed
- **IAM role on EC2** — `AmazonEC2ContainerRegistryReadOnly` (pulls images without credentials on the instance)
- **IAM user for CI** — ECR push permissions only
- **Caddy** — reverse proxy on EC2 for HTTPS + automatic Let's Encrypt cert

## Design notes

- `KanboardClient` uses `java.net.http.HttpClient` — no extra HTTP libraries
- Tools return `JsonNode` pretty-printed as strings — no model POJOs needed
- Java records for all DTOs
- Credentials via env vars only — never hardcoded
