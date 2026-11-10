package eu.howarth.mcp.kanban.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.howarth.mcp.kanban.client.KanboardClient;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kanboard's getBoard returns every card, closed ones included, each with about
 * 60 fields and its full description. On a board that closes cards in place that
 * is megabytes, more than an MCP result can carry. So the tool returns open cards
 * with summary fields unless asked otherwise.
 */
class GetBoardTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** One lane, one column, an open card (#1) and a closed card (#2), as Kanboard sends them. */
    private static final String BOARD = """
            [ {
              "id" : 9, "name" : "General", "nb_tasks" : 2,
              "columns" : [ {
                "id" : 34, "title" : "Done", "nb_open_tasks" : 1, "nb_tasks" : 2,
                "tasks" : [ {
                  "id" : 1, "title" : "Open card", "description" : "A long description",
                  "is_active" : 1, "column_id" : 34, "column_name" : "Done",
                  "swimlane_id" : 9, "swimlane_name" : "General",
                  "owner_id" : 29, "assignee_username" : "claude",
                  "category_id" : 8, "category_name" : "Software & Portfolio",
                  "color_id" : "blue", "priority" : 0, "position" : 1,
                  "date_due" : 0, "date_creation" : 100, "date_moved" : 200, "date_modification" : 300,
                  "nb_comments" : 2, "nb_subtasks" : 3, "nb_completed_subtasks" : 1, "nb_links" : 4,
                  "recurrence_status" : 0, "assignee_avatar_path" : null,
                  "tags" : [ { "id" : 98, "name" : "quick-win", "color_id" : "green", "task_id" : 1 } ]
                }, {
                  "id" : 2, "title" : "Closed card", "description" : "Old", "is_active" : 0,
                  "column_id" : 34, "tags" : [ ]
                } ]
              } ]
            } ]
            """;

    static class BoardClient extends KanboardClient {
        BoardClient() {
            super(null, null, MAPPER);
        }

        @Override
        public JsonNode execute(String method, Map<String, Object> params) {
            try {
                return MAPPER.readTree(BOARD);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static JsonNode tasks(String json) throws Exception {
        return MAPPER.readTree(json).get(0).get("columns").get(0).get("tasks");
    }

    private static List<Integer> ids(JsonNode tasks) {
        List<Integer> ids = new ArrayList<>();
        tasks.forEach(task -> ids.add(task.get("id").asInt()));
        return ids;
    }

    @Test
    void closedCardsAreLeftOutByDefault() throws Exception {
        String board = new BoardTools(new BoardClient()).getBoard(7, null, null);
        assertEquals(List.of(1), ids(tasks(board)));
    }

    @Test
    void closedCardsComeBackWhenAskedFor() throws Exception {
        String board = new BoardTools(new BoardClient()).getBoard(7, true, null);
        assertEquals(List.of(1, 2), ids(tasks(board)));
    }

    @Test
    void lanesAndColumnsKeepTheirOwnFields() throws Exception {
        JsonNode lane = MAPPER.readTree(new BoardTools(new BoardClient()).getBoard(7, null, null)).get(0);
        assertEquals("General", lane.get("name").asText());
        assertEquals("Done", lane.get("columns").get(0).get("title").asText());
        assertEquals(1, lane.get("columns").get(0).get("nb_open_tasks").asInt());
    }

    @Test
    void cardsCarrySummaryFieldsByDefault() throws Exception {
        JsonNode card = tasks(new BoardTools(new BoardClient()).getBoard(7, null, null)).get(0);
        assertFalse(card.has("description"));
        assertFalse(card.has("recurrence_status"));
        assertFalse(card.has("assignee_avatar_path"));
        assertEquals("Open card", card.get("title").asText());
        assertFalse(card.has("column_name"), "the card sits inside its column");
        assertFalse(card.has("swimlane_name"), "and inside its swimlane");
        assertEquals("claude", card.get("assignee_username").asText());
        assertEquals("Software & Portfolio", card.get("category_name").asText());
        assertEquals(200, card.get("date_moved").asInt());
        assertEquals(100, card.get("date_creation").asInt());
        assertEquals(0, card.get("date_due").asInt());
        assertEquals(2, card.get("nb_comments").asInt());
        assertEquals(3, card.get("nb_subtasks").asInt());
        assertEquals(4, card.get("nb_links").asInt());
    }

    @Test
    void tagsAreNamesOnlyInTheSummary() throws Exception {
        JsonNode tags = tasks(new BoardTools(new BoardClient()).getBoard(7, null, null)).get(0).get("tags");
        assertEquals(1, tags.size());
        assertEquals("quick-win", tags.get(0).asText());
    }

    @Test
    void fullDetailReturnsEveryKanboardField() throws Exception {
        JsonNode card = tasks(new BoardTools(new BoardClient()).getBoard(7, null, true)).get(0);
        assertEquals("A long description", card.get("description").asText());
        assertTrue(card.has("recurrence_status"));
        assertEquals("quick-win", card.get("tags").get(0).get("name").asText());
    }

    @Test
    void fullDetailStillLeavesClosedCardsOut() throws Exception {
        String board = new BoardTools(new BoardClient()).getBoard(7, null, true);
        assertEquals(List.of(1), ids(tasks(board)));
    }
}
