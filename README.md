# KanbanApi — Kanboard MCP Server

A Spring Boot MCP (Model Context Protocol) server that bridges Claude Code to your Kanboard instance. Lets Claude read boards, manage tasks, add comments, and move cards.

## Tech Stack

- **Java 17** (Temurin 17 via `~/.mavenrc`)
- **Spring Boot 3.4.3** with **Spring AI 1.0.0** (`spring-ai-starter-mcp-server-webmvc`)
- **Maven 3.9.11** (wrapper included)
- **HTTP Streamable transport** on `localhost:8080/mcp`

## Project Structure

```
KanbanApi/
├── pom.xml
├── mvnw / mvnw.cmd / .mvn/wrapper/
├── .mcp.json
├── src/main/java/com/kanbanapi/
│   ├── KanbanApiApplication.java
│   ├── config/
│   │   ├── KanboardProperties.java
│   │   └── McpToolConfig.java
│   ├── client/
│   │   ├── KanboardClient.java
│   │   ├── JsonRpcRequest.java
│   │   ├── JsonRpcResponse.java
│   │   └── KanboardException.java
│   └── tools/
│       ├── ProjectTools.java
│       ├── BoardTools.java
│       ├── TaskTools.java
│       └── CommentTools.java
├── src/main/resources/
│   └── application.yml
└── src/test/java/com/kanbanapi/
    └── KanbanApiApplicationTests.java
```

## MCP Tools (13)

| Service | Tool | Kanboard Method |
|---------|------|-----------------|
| ProjectTools | `getAllProjects` | getAllProjects |
| ProjectTools | `getProjectById` | getProjectById |
| BoardTools | `getBoard` | getBoard |
| BoardTools | `getColumns` | getColumns |
| BoardTools | `moveTaskPosition` | moveTaskPosition |
| TaskTools | `searchTasks` | searchTasks |
| TaskTools | `getTask` | getTask |
| TaskTools | `createTask` | createTask |
| TaskTools | `updateTask` | updateTask |
| TaskTools | `closeTask` | closeTask |
| TaskTools | `openTask` | openTask |
| CommentTools | `getAllComments` | getAllComments |
| CommentTools | `createComment` | createComment |

## Design Decisions

- **`KanboardClient`** uses `java.net.http.HttpClient` — no extra HTTP libraries needed
- **Returns `JsonNode`** from the client, pretty-printed as strings by tools — avoids dozens of model POJOs
- **Java records** for DTOs — clean, immutable data carriers
- **Credentials via env vars** with defaults in `application.yml` — never hardcoded
