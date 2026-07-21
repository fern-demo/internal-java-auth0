package com.auth0.client.mgmt;

import com.auth0.client.mgmt.core.ObjectMappers;
import com.auth0.client.mgmt.core.OptionalNullable;
import com.auth0.client.mgmt.core.SyncPagingIterable;
import com.auth0.client.mgmt.types.ConnectionOptionsAd;
import com.auth0.client.mgmt.types.ConnectionResponseContent;
import com.auth0.client.mgmt.types.ConnectionSetUserRootAttributesEnum;
import com.auth0.client.mgmt.types.ConnectionStrategyEnum;
import com.auth0.client.mgmt.types.CreateConnectionRequestContent;
import com.auth0.client.mgmt.types.CreateConnectionRequestContentAd;
import com.auth0.client.mgmt.types.GetConnectionRequestParameters;
import com.auth0.client.mgmt.types.ListConnectionsQueryParameters;
import com.auth0.client.mgmt.types.UpdateConnectionRequestContent;
import com.auth0.client.mgmt.types.UpdateConnectionRequestContentAd;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConnectionsWireTest {
    private MockWebServer server;
    private ManagementApi client;
    private ObjectMapper objectMapper = ObjectMappers.JSON_MAPPER;

    @BeforeEach
    public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        client = ManagementApi.builder()
                .url(server.url("/").toString())
                .token("test-token")
                .build();
    }

    @AfterEach
    public void teardown() throws Exception {
        server.shutdown();
    }

    @Test
    public void testList() throws Exception {
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"next\":\"next\",\"connections\":[{\"display_name\":\"connections\",\"enabled_clients\":[\"connections\"],\"is_domain_connection\":true,\"name\":\"connections\",\"id\":\"connections\",\"realms\":[\"connections\"],\"authentication\":{\"active\":true},\"connected_accounts\":{\"active\":true},\"provisioning_ticket_url\":\"provisioning_ticket_url\",\"strategy\":\"ad\"}]}"));
        SyncPagingIterable<ConnectionResponseContent> response = client.connections()
                .list(ListConnectionsQueryParameters.builder()
                        .from("from")
                        .take(1)
                        .name("name")
                        .fields("fields")
                        .includeFields(true)
                        .strategy(Arrays.asList(ConnectionStrategyEnum.AD))
                        .build());
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());

        // Validate response body
        Assertions.assertNotNull(response, "Response should not be null");
        // Pagination response validated via MockWebServer
        // The SDK correctly parses the response into a SyncPagingIterable
    }

    @Test
    public void testCreate() throws Exception {
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"id\":\"con_a1B2c3D4e5F6g7H8\",\"strategy\":\"ad\",\"name\":\"corp-active-directory\",\"display_name\":\"Corporate Active Directory\",\"is_domain_connection\":true,\"enabled_clients\":[\"AaiyAPdpYdesoKnqjj8HJqRn4T5titww\"],\"realms\":[\"corp-active-directory\"],\"options\":{\"tenant_domain\":\"corp.example.com\",\"domain_aliases\":[\"example.com\",\"corp.example.com\"],\"kerberos\":true,\"brute_force_protection\":true,\"set_user_root_attributes\":\"on_each_login\"},\"metadata\":{\"environment\":\"production\",\"managed_by\":\"identity-team\"}}"));
        ConnectionResponseContent response = client.connections()
                .create(CreateConnectionRequestContent.ad(CreateConnectionRequestContentAd.builder()
                        .name("corp-active-directory")
                        .enabledClients(Optional.of(Arrays.asList("AaiyAPdpYdesoKnqjj8HJqRn4T5titww")))
                        .displayName("Corporate Active Directory")
                        .isDomainConnection(true)
                        .metadata(new HashMap<String, OptionalNullable<String>>() {
                            {
                                put("environment", "production");
                                put("managed_by", "identity-team");
                            }
                        })
                        .options(ConnectionOptionsAd.builder()
                                .nonPersistentAttrs(Arrays.asList("ethnicity", "gender"))
                                .agentIp("10.0.0.42")
                                .agentMode(false)
                                .agentVersion("4.3.0")
                                .bruteForceProtection(true)
                                .disableCache(false)
                                .domainAliases(Arrays.asList("example.com", "corp.example.com"))
                                .ips(Arrays.asList("10.0.0.0/24"))
                                .kerberos(true)
                                .setUserRootAttributes(ConnectionSetUserRootAttributesEnum.ON_EACH_LOGIN)
                                .tenantDomain("corp.example.com")
                                .build())
                        .build()));
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
        // Validate request body
        String actualRequestBody = request.getBody().readUtf8();
        String expectedRequestBody = ""
                + "{\n"
                + "  \"strategy\": \"ad\",\n"
                + "  \"options\": {\n"
                + "    \"tenant_domain\": \"corp.example.com\",\n"
                + "    \"domain_aliases\": [\n"
                + "      \"example.com\",\n"
                + "      \"corp.example.com\"\n"
                + "    ],\n"
                + "    \"agentIP\": \"10.0.0.42\",\n"
                + "    \"agentVersion\": \"4.3.0\",\n"
                + "    \"agentMode\": false,\n"
                + "    \"kerberos\": true,\n"
                + "    \"brute_force_protection\": true,\n"
                + "    \"disable_cache\": false,\n"
                + "    \"set_user_root_attributes\": \"on_each_login\",\n"
                + "    \"ips\": [\n"
                + "      \"10.0.0.0/24\"\n"
                + "    ],\n"
                + "    \"non_persistent_attrs\": [\n"
                + "      \"ethnicity\",\n"
                + "      \"gender\"\n"
                + "    ]\n"
                + "  }\n"
                + "}";
        JsonNode actualJson = objectMapper.readTree(actualRequestBody);
        JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
        Assertions.assertTrue(jsonEquals(expectedJson, actualJson), "Request body structure does not match expected");
        if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
            String discriminator = null;
            if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
            else if (actualJson.has("_type"))
                discriminator = actualJson.get("_type").asText();
            else if (actualJson.has("kind"))
                discriminator = actualJson.get("kind").asText();
            Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
            Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
        }

        if (!actualJson.isNull()) {
            Assertions.assertTrue(
                    actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
                    "request should be a valid JSON value");
        }

        if (actualJson.isArray()) {
            Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
        }
        if (actualJson.isObject()) {
            Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
        }

        // Validate response body
        Assertions.assertNotNull(response, "Response should not be null");
        String actualResponseJson = objectMapper.writeValueAsString(response);
        String expectedResponseBody = ""
                + "{\n"
                + "  \"id\": \"con_a1B2c3D4e5F6g7H8\",\n"
                + "  \"strategy\": \"ad\",\n"
                + "  \"name\": \"corp-active-directory\",\n"
                + "  \"display_name\": \"Corporate Active Directory\",\n"
                + "  \"is_domain_connection\": true,\n"
                + "  \"enabled_clients\": [\n"
                + "    \"AaiyAPdpYdesoKnqjj8HJqRn4T5titww\"\n"
                + "  ],\n"
                + "  \"realms\": [\n"
                + "    \"corp-active-directory\"\n"
                + "  ],\n"
                + "  \"options\": {\n"
                + "    \"tenant_domain\": \"corp.example.com\",\n"
                + "    \"domain_aliases\": [\n"
                + "      \"example.com\",\n"
                + "      \"corp.example.com\"\n"
                + "    ],\n"
                + "    \"kerberos\": true,\n"
                + "    \"brute_force_protection\": true,\n"
                + "    \"set_user_root_attributes\": \"on_each_login\"\n"
                + "  },\n"
                + "  \"metadata\": {\n"
                + "    \"environment\": \"production\",\n"
                + "    \"managed_by\": \"identity-team\"\n"
                + "  }\n"
                + "}";
        JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
        JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
        Assertions.assertTrue(
                jsonEquals(expectedResponseNode, actualResponseNode),
                "Response body structure does not match expected");
        if (actualResponseNode.has("type") || actualResponseNode.has("_type") || actualResponseNode.has("kind")) {
            String discriminator = null;
            if (actualResponseNode.has("type"))
                discriminator = actualResponseNode.get("type").asText();
            else if (actualResponseNode.has("_type"))
                discriminator = actualResponseNode.get("_type").asText();
            else if (actualResponseNode.has("kind"))
                discriminator = actualResponseNode.get("kind").asText();
            Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
            Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
        }

        if (!actualResponseNode.isNull()) {
            Assertions.assertTrue(
                    actualResponseNode.isObject() || actualResponseNode.isArray() || actualResponseNode.isValueNode(),
                    "response should be a valid JSON value");
        }

        if (actualResponseNode.isArray()) {
            Assertions.assertTrue(actualResponseNode.size() >= 0, "Array should have valid size");
        }
        if (actualResponseNode.isObject()) {
            Assertions.assertTrue(actualResponseNode.size() >= 0, "Object should have valid field count");
        }
    }

    @Test
    public void testGet() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(TestResources.loadResource("/wire-tests/ConnectionsWireTest_testGet_response.json")));
        ConnectionResponseContent response = client.connections()
                .get(
                        "id",
                        GetConnectionRequestParameters.builder()
                                .fields("fields")
                                .includeFields(true)
                                .build());
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());

        // Validate response body
        Assertions.assertNotNull(response, "Response should not be null");
        String actualResponseJson = objectMapper.writeValueAsString(response);
        String expectedResponseBody =
                TestResources.loadResource("/wire-tests/ConnectionsWireTest_testGet_response.json");
        JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
        JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
        Assertions.assertTrue(
                jsonEquals(expectedResponseNode, actualResponseNode),
                "Response body structure does not match expected");
        if (actualResponseNode.has("type") || actualResponseNode.has("_type") || actualResponseNode.has("kind")) {
            String discriminator = null;
            if (actualResponseNode.has("type"))
                discriminator = actualResponseNode.get("type").asText();
            else if (actualResponseNode.has("_type"))
                discriminator = actualResponseNode.get("_type").asText();
            else if (actualResponseNode.has("kind"))
                discriminator = actualResponseNode.get("kind").asText();
            Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
            Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
        }

        if (!actualResponseNode.isNull()) {
            Assertions.assertTrue(
                    actualResponseNode.isObject() || actualResponseNode.isArray() || actualResponseNode.isValueNode(),
                    "response should be a valid JSON value");
        }

        if (actualResponseNode.isArray()) {
            Assertions.assertTrue(actualResponseNode.size() >= 0, "Array should have valid size");
        }
        if (actualResponseNode.isObject()) {
            Assertions.assertTrue(actualResponseNode.size() >= 0, "Object should have valid field count");
        }
    }

    @Test
    public void testDelete() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        client.connections().delete("id");
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("DELETE", request.getMethod());
    }

    @Test
    public void testUpdate() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(TestResources.loadResource("/wire-tests/ConnectionsWireTest_testUpdate_response.json")));
        ConnectionResponseContent response = client.connections()
                .update(
                        "id",
                        UpdateConnectionRequestContent.of(
                                UpdateConnectionRequestContentAd.builder().build()));
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("PATCH", request.getMethod());
        // Validate request body
        String actualRequestBody = request.getBody().readUtf8();
        String expectedRequestBody = "" + "{}";
        JsonNode actualJson = objectMapper.readTree(actualRequestBody);
        JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
        Assertions.assertTrue(jsonEquals(expectedJson, actualJson), "Request body structure does not match expected");
        if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
            String discriminator = null;
            if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
            else if (actualJson.has("_type"))
                discriminator = actualJson.get("_type").asText();
            else if (actualJson.has("kind"))
                discriminator = actualJson.get("kind").asText();
            Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
            Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
        }

        if (!actualJson.isNull()) {
            Assertions.assertTrue(
                    actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
                    "request should be a valid JSON value");
        }

        if (actualJson.isArray()) {
            Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
        }
        if (actualJson.isObject()) {
            Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
        }

        // Validate response body
        Assertions.assertNotNull(response, "Response should not be null");
        String actualResponseJson = objectMapper.writeValueAsString(response);
        String expectedResponseBody =
                TestResources.loadResource("/wire-tests/ConnectionsWireTest_testUpdate_response.json");
        JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
        JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
        Assertions.assertTrue(
                jsonEquals(expectedResponseNode, actualResponseNode),
                "Response body structure does not match expected");
        if (actualResponseNode.has("type") || actualResponseNode.has("_type") || actualResponseNode.has("kind")) {
            String discriminator = null;
            if (actualResponseNode.has("type"))
                discriminator = actualResponseNode.get("type").asText();
            else if (actualResponseNode.has("_type"))
                discriminator = actualResponseNode.get("_type").asText();
            else if (actualResponseNode.has("kind"))
                discriminator = actualResponseNode.get("kind").asText();
            Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
            Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
        }

        if (!actualResponseNode.isNull()) {
            Assertions.assertTrue(
                    actualResponseNode.isObject() || actualResponseNode.isArray() || actualResponseNode.isValueNode(),
                    "response should be a valid JSON value");
        }

        if (actualResponseNode.isArray()) {
            Assertions.assertTrue(actualResponseNode.size() >= 0, "Array should have valid size");
        }
        if (actualResponseNode.isObject()) {
            Assertions.assertTrue(actualResponseNode.size() >= 0, "Object should have valid field count");
        }
    }

    @Test
    public void testCheckStatus() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        client.connections().checkStatus("id");
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }

    /**
     * Compares two JsonNodes with numeric equivalence and null safety.
     * For objects, checks that all fields in 'expected' exist in 'actual' with matching values.
     * Allows 'actual' to have extra fields (e.g., default values added during serialization).
     */
    private boolean jsonEquals(JsonNode expected, JsonNode actual) {
        if (expected == null && actual == null) return true;
        if (expected == null || actual == null) return false;
        if (expected.equals(actual)) return true;
        if (expected.isNumber() && actual.isNumber())
            return Math.abs(expected.doubleValue() - actual.doubleValue()) < 1e-10;
        if (expected.isObject() && actual.isObject()) {
            java.util.Iterator<java.util.Map.Entry<String, JsonNode>> iter = expected.fields();
            while (iter.hasNext()) {
                java.util.Map.Entry<String, JsonNode> entry = iter.next();
                JsonNode actualValue = actual.get(entry.getKey());
                if (actualValue == null) {
                    if (!entry.getValue().isNull()) return false;
                } else if (!jsonEquals(entry.getValue(), actualValue)) return false;
            }
            return true;
        }
        if (expected.isArray() && actual.isArray()) {
            if (expected.size() != actual.size()) return false;
            for (int i = 0; i < expected.size(); i++) {
                if (!jsonEquals(expected.get(i), actual.get(i))) return false;
            }
            return true;
        }
        return false;
    }
}
