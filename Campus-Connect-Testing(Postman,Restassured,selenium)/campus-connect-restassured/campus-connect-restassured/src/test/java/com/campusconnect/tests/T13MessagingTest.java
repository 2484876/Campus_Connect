package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 13 - Messaging
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T13MessagingTest extends BaseTest {

    @Test(priority = 1, description = "U1 send DM to U2")
    public void U1_send_DM_to_U2() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"receiverId\": \"{{user2Id}}\",\n  \"content\": \"Hi Bob\",\n  \"messageType\": \"TEXT\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/messages"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "messageId");
    }

    @Test(priority = 2, description = "U1 get conversation with U2")
    public void U1_get_conversation_with_U2() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/messages/{{user2Id}}?page=0&size=50"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "U1 list conversations")
    public void U1_list_conversations() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/conversations"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "U2 mark read from U1")
    public void U2_mark_read_from_U1() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().put(url("{{baseUrl}}/api/messages/read/{{user1Id}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "U1 edit message")
    public void U1_edit_message() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"content\": \"Hi Bob (edited)\"\n}");
        Response r = spec.when().put(url("{{baseUrl}}/api/messages/{{messageId}}/edit"));
        assertStatus(r, 200);
    }

    @Test(priority = 6, description = "U1 pin message")
    public void U1_pin_message() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/messages/{{messageId}}/pin"));
        assertStatus(r, 200);
    }

    @Test(priority = 7, description = "Get pinned DM")
    public void Get_pinned_DM() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/pinned/dm/{{user2Id}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 8, description = "U1 unpin message")
    public void U1_unpin_message() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().delete(url("{{baseUrl}}/api/messages/{{messageId}}/pin"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 9, description = "U2 react to message")
    public void U2_react_to_message() {
        RequestSpecification spec = auth("token2");
        spec = json(spec, "{\n  \"messageId\": \"{{messageId}}\",\n  \"emoji\": \"\\ud83d\\udc4d\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/messages/react"));
        assertStatus(r, 200, 201);
    }

    @Test(priority = 10, description = "Get message reactions")
    public void Get_message_reactions() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/messages/{{messageId}}/reactions"));
        assertStatus(r, 200);
    }

    @Test(priority = 11, description = "Search messages")
    public void Search_messages() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/messages/search?q=Bob"));
        assertStatus(r, 200);
    }

    @Test(priority = 12, description = "U1 typing indicator")
    public void U1_typing_indicator() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"receiverId\": \"{{user2Id}}\",\n  \"typing\": true\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/messages/typing"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 13, description = "U1 create room with U2")
    public void U1_create_room_with_U2() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"name\": \"Test Room {{runId}}\",\n  \"memberIds\": [\n    \"{{user2Id}}\"\n  ]\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/rooms"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "roomId");
    }

    @Test(priority = 14, description = "U1 list rooms")
    public void U1_list_rooms() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/rooms"));
        assertStatus(r, 200);
    }

    @Test(priority = 15, description = "Get room")
    public void Get_room() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/rooms/{{roomId}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 16, description = "U1 send room message")
    public void U1_send_room_message() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"chatRoomId\": \"{{roomId}}\",\n  \"content\": \"Hello room\",\n  \"messageType\": \"TEXT\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/messages"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "roomMessageId");
    }

    @Test(priority = 17, description = "Get room messages")
    public void Get_room_messages() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/rooms/{{roomId}}/messages?page=0&size=50"));
        assertStatus(r, 200);
    }

    @Test(priority = 18, description = "Get pinned room")
    public void Get_pinned_room() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/pinned/room/{{roomId}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 19, description = "U1 rename room")
    public void U1_rename_room() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"name\": \"Renamed Room\"\n}");
        Response r = spec.when().put(url("{{baseUrl}}/api/rooms/{{roomId}}/rename"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 20, description = "U1 mark room read")
    public void U1_mark_room_read() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().put(url("{{baseUrl}}/api/rooms/{{roomId}}/read"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 21, description = "U1 delete message for everyone")
    public void U1_delete_message_for_everyone() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"messageId\": \"{{roomMessageId}}\",\n  \"deleteType\": \"FOR_EVERYONE\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/messages/delete"));
        assertStatus(r, 200, 201);
    }

    @Test(priority = 22, description = "U2 leave room")
    public void U2_leave_room() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().post(url("{{baseUrl}}/api/rooms/{{roomId}}/leave"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 23, description = "NEG Send DM no token")
    public void NEG_Send_DM_no_token() {
        RequestSpecification spec = noAuth();
        spec = json(spec, "{\n  \"receiverId\": \"{{user2Id}}\",\n  \"content\": \"x\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/messages"));
        assertStatus(r, 401, 403);
    }
}
