package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 10 - Events & Event Chat
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T10EventsEventChatTest extends BaseTest {

    @Test(priority = 1, description = "U1 create event")
    public void U1_create_event() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"title\": \"Test Conference\",\n  \"description\": \"An automated test event\",\n  \"eventDate\": \"2030-06-01T10:00:00\",\n  \"eventEndDate\": \"2030-06-01T12:00:00\",\n  \"location\": \"Main Hall\",\n  \"eventType\": \"PHYSICAL\",\n  \"category\": \"CONFERENCE\",\n  \"maxParticipants\": 100,\n  \"showAttendees\": true\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/events"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "eventId");
    }

    @Test(priority = 2, description = "Update event")
    public void Update_event() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"title\": \"Test Conference (updated)\",\n  \"eventDate\": \"2030-06-01T10:00:00\",\n  \"eventType\": \"PHYSICAL\",\n  \"category\": \"CONFERENCE\"\n}");
        Response r = spec.when().put(url("{{baseUrl}}/api/events/{{eventId}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "List events")
    public void List_events() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/events?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "Upcoming events")
    public void Upcoming_events() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/events/upcoming?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "Past events")
    public void Past_events() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/events/past?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 6, description = "This week events")
    public void This_week_events() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/events/this-week?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 7, description = "Events by category")
    public void Events_by_category() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/events/category/CONFERENCE?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 8, description = "My events")
    public void My_events() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/events/mine?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 9, description = "Get single event")
    public void Get_single_event() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/events/{{eventId}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 10, description = "U2 RSVP going")
    public void U2_RSVP_going() {
        RequestSpecification spec = auth("token2");
        spec = json(spec, "{\n  \"status\": \"GOING\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/events/{{eventId}}/rsvp"));
        assertStatus(r, 200);
    }

    @Test(priority = 11, description = "Event attendees")
    public void Event_attendees() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/events/{{eventId}}/attendees?status=GOING"));
        assertStatus(r, 200);
    }

    @Test(priority = 12, description = "Event chat access")
    public void Event_chat_access() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().get(url("{{baseUrl}}/api/events/{{eventId}}/chat/access"));
        assertStatus(r, 200);
    }

    @Test(priority = 13, description = "U2 send event chat")
    public void U2_send_event_chat() {
        RequestSpecification spec = auth("token2");
        spec = json(spec, "{\n  \"content\": \"Excited for this event!\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/events/{{eventId}}/chat"));
        assertStatus(r, 200, 201);
    }

    @Test(priority = 14, description = "Get event chat messages")
    public void Get_event_chat_messages() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().get(url("{{baseUrl}}/api/events/{{eventId}}/chat?page=0&size=50"));
        assertStatus(r, 200);
    }

    @Test(priority = 15, description = "U2 remove RSVP")
    public void U2_remove_RSVP() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().delete(url("{{baseUrl}}/api/events/{{eventId}}/rsvp"));
        assertStatus(r, 200, 204);
    }

    @Test(priority = 16, description = "NEG Create event missing title")
    public void NEG_Create_event_missing_title() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"description\": \"no title\",\n  \"eventDate\": \"2030-06-01T10:00:00\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/events"));
        assertStatus(r, 400);
    }

    @Test(priority = 17, description = "NEG U2 update U1 event")
    public void NEG_U2_update_U1_event() {
        RequestSpecification spec = auth("token2");
        spec = json(spec, "{\n  \"title\": \"hijack\",\n  \"eventDate\": \"2030-06-01T10:00:00\"\n}");
        Response r = spec.when().put(url("{{baseUrl}}/api/events/{{eventId}}"));
        assertStatus(r, 400, 403);
    }
}
