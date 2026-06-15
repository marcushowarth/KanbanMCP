package eu.howarth.mcp.kanban.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record JsonRpcResponse(
        String jsonrpc,
        int id,
        JsonNode result,
        JsonNode error
) {
}
