package eu.howarth.mcp.kanban.client;

public class KanboardException extends RuntimeException {

    public KanboardException(String message) {
        super(message);
    }

    public KanboardException(String message, Throwable cause) {
        super(message, cause);
    }
}
