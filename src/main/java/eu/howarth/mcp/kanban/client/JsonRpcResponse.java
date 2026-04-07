package eu.howarth.mcp.kanban.client;

import tools.jackson.databind.JsonNode;

public record JsonRpcResponse(
        String jsonrpc,
        int id,
        JsonNode result,
        JsonNode error
) {
}
