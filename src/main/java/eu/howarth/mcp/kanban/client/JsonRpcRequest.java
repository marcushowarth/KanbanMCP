package eu.howarth.mcp.kanban.client;

import java.util.Map;

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
