package eu.howarth.mcp.kanban.client;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Map;

@RegisterForReflection
public record JsonRpcRequest(
        String jsonrpc,
        String method,
        int id,
        Map<String, Object> params
) {
    public JsonRpcRequest(String method, int id, Map<String, Object> params) {
        this("2.0", method, id, params);
    }

    public JsonRpcRequest(String method, int id) {
        this(method, id, Map.of());
    }
}
