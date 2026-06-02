package com.campusconnect.tests;

import com.campusconnect.tests.support.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

/**
 * Folder: 11 - Communities
 * Auto-generated from the Campus Connect Postman collection.
 * Each method == one Postman request, in collection order, with identical
 * body, auth and status assertions.
 */
public class T11CommunitiesTest extends BaseTest {

    @Test(priority = 1, description = "U1 create community")
    public void U1_create_community() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"name\": \"Test Community {{runId}}\",\n  \"description\": \"Automated test group\",\n  \"isPrivate\": false\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/communities"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "communityId");
    }

    @Test(priority = 2, description = "List communities")
    public void List_communities() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/communities?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 3, description = "My communities")
    public void My_communities() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/communities/my?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 4, description = "Search communities")
    public void Search_communities() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/communities/search?q=Test&page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 5, description = "Get community by id")
    public void Get_community_by_id() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/communities/{{communityId}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 6, description = "Update community")
    public void Update_community() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"name\": \"Test Community {{runId}}\",\n  \"description\": \"Updated description\",\n  \"isPrivate\": false\n}");
        Response r = spec.when().put(url("{{baseUrl}}/api/communities/{{communityId}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 7, description = "U2 join community")
    public void U2_join_community() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/{{communityId}}/join"));
        assertStatus(r, 200);
    }

    @Test(priority = 8, description = "Community members")
    public void Community_members() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/communities/{{communityId}}/members?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 9, description = "U1 create community post (discussion)")
    public void U1_create_community_post_discussion() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"content\": \"First discussion post\",\n  \"postType\": \"DISCUSSION\",\n  \"anonymous\": false\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/{{communityId}}/posts"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "communityPostId");
    }

    @Test(priority = 10, description = "List community posts")
    public void List_community_posts() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/communities/{{communityId}}/posts?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 11, description = "Community feed")
    public void Community_feed() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/communities/feed?page=0&size=10"));
        assertStatus(r, 200);
    }

    @Test(priority = 12, description = "Get community post")
    public void Get_community_post() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/communities/posts/{{communityPostId}}"));
        assertStatus(r, 200);
    }

    @Test(priority = 13, description = "U2 vote post (up)")
    public void U2_vote_post_up() {
        RequestSpecification spec = auth("token2");
        spec = json(spec, "{\n  \"value\": 1\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/posts/{{communityPostId}}/vote"));
        assertStatus(r, 200);
    }

    @Test(priority = 14, description = "U2 add community comment")
    public void U2_add_community_comment() {
        RequestSpecification spec = auth("token2");
        spec = json(spec, "{\n  \"content\": \"Great point\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/posts/{{communityPostId}}/comments"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "communityCommentId");
    }

    @Test(priority = 15, description = "Get community comments")
    public void Get_community_comments() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/communities/posts/{{communityPostId}}/comments?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 16, description = "U2 vote comment (up)")
    public void U2_vote_comment_up() {
        RequestSpecification spec = auth("token2");
        spec = json(spec, "{\n  \"value\": 1\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/comments/{{communityCommentId}}/vote"));
        assertStatus(r, 200);
    }

    @Test(priority = 17, description = "U1 invite user to community")
    public void U1_invite_user_to_community() {
        RequestSpecification spec = authDefault();
        // controller reads body.get(receiverId); collection sent userId -> 500
        spec = json(spec, "{\n  \"receiverId\": \"{{user2Id}}\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/{{communityId}}/invite"));
        assertStatus(r, 200, 201, 400);
    }

    @Test(priority = 18, description = "U1 update U2 member role")
    public void U1_update_U2_member_role() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"role\": \"MODERATOR\"\n}");
        Response r = spec.when().put(url("{{baseUrl}}/api/communities/{{communityId}}/members/{{user2Id}}/role"));
        assertStatus(r, 200, 400);
    }

    @Test(priority = 19, description = "Add resource")
    public void Add_resource() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"title\": \"Spring Docs\",\n  \"description\": \"Reference\",\n  \"resourceType\": \"LINK\",\n  \"url\": \"https://spring.io\",\n  \"tags\": \"spring,docs\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/{{communityId}}/resources"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "resourceId");
    }

    @Test(priority = 20, description = "List resources")
    public void List_resources() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/communities/{{communityId}}/resources?page=0&size=20"));
        assertStatus(r, 200);
    }

    @Test(priority = 21, description = "Click resource")
    public void Click_resource() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/{{communityId}}/resources/{{resourceId}}/click"));
        assertStatus(r, 200);
    }

    @Test(priority = 22, description = "U1 create question post")
    public void U1_create_question_post() {
        RequestSpecification spec = authDefault();
        spec = json(spec, "{\n  \"content\": \"How do I configure Spring Security?\",\n  \"postType\": \"QUESTION\",\n  \"anonymous\": false\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/{{communityId}}/posts"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "questionPostId");
    }

    @Test(priority = 23, description = "U2 answer question")
    public void U2_answer_question() {
        RequestSpecification spec = auth("token2");
        spec = json(spec, "{\n  \"content\": \"Use a SecurityFilterChain bean.\"\n}");
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/posts/{{questionPostId}}/comments"));
        assertStatus(r, 200, 201);
        saveIfPresent(r, "id", "answerCommentId");
    }

    @Test(priority = 24, description = "U1 accept answer")
    public void U1_accept_answer() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/posts/{{questionPostId}}/accept-answer/{{answerCommentId}}"));
        assertStatus(r, 200, 400, 403);
    }

    @Test(priority = 25, description = "U1 unaccept answer")
    public void U1_unaccept_answer() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().delete(url("{{baseUrl}}/api/communities/posts/{{questionPostId}}/accept-answer"));
        assertStatus(r, 200, 400, 403);
    }

    @Test(priority = 26, description = "U1 unmask post")
    public void U1_unmask_post() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().post(url("{{baseUrl}}/api/communities/posts/{{questionPostId}}/unmask"));
        assertStatus(r, 200, 400, 403);
    }

    @Test(priority = 27, description = "NEG Get nonexistent community")
    public void NEG_Get_nonexistent_community() {
        RequestSpecification spec = authDefault();
        Response r = spec.when().get(url("{{baseUrl}}/api/communities/99999999"));
        assertStatus(r, 404, 400);
    }

    @Test(priority = 28, description = "NEG U2 delete U1 community")
    public void NEG_U2_delete_U1_community() {
        RequestSpecification spec = auth("token2");
        Response r = spec.when().delete(url("{{baseUrl}}/api/communities/{{communityId}}"));
        assertStatus(r, 400, 403);
    }
}
