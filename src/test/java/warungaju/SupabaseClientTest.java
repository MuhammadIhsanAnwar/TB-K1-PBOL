package warungaju;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SupabaseClientTest {
    private static final String USER_ID = "c902bc66-ea32-4bd0-84d4-f937b2936f4a";
    private final List<Request> requests = new ArrayList<>();
    private HttpServer server;
    private SupabaseClient client;

    @BeforeEach
    void startSupabaseApiStub() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
        client = new SupabaseClient("http://127.0.0.1:" + server.getAddress().getPort(),
            "test-publishable-key");
    }

    @AfterEach
    void stopSupabaseApiStub() {
        server.stop(0);
    }

    @Test
    void connectionCheckMeasuresSupabaseHealthRequest() throws Exception {
        SupabaseClient.ConnectionCheck result = client.checkConnection();

        assertEquals(204, result.httpStatus);
        assertTrue(result.elapsedMillis >= 0);
        assertEquals("/auth/v1/health", requests.get(0).path);
        assertEquals("GET", requests.get(0).method);
        assertEquals("test-publishable-key", requests.get(0).apiKey);
    }

    @Test
    void authErrorsExposeHttpStatusAndSafeProviderCode() {
        IOException error = assertThrows(IOException.class,
            () -> client.signIn("pemilik@example.id", "wrong-password"));

        assertTrue(error.getMessage().contains("HTTP 400"));
        assertTrue(error.getMessage().contains("[invalid_credentials]"));
        assertTrue(error.getMessage().contains("Invalid login credentials"));
    }

    @Test
    void profileReadUsesPostgrestAndTheSignedInUserToken() throws Exception {
        AppUser profile = client.getProfile(session());

        assertEquals("Usaha Kopi", profile.fullName);
        assertEquals("umkm", profile.role);
        assertTrue(profile.active);
        assertEquals("/rest/v1/users", requests.get(0).path);
        assertTrue(requests.get(0).query.contains("id=eq." + USER_ID));
        assertEquals("test-publishable-key", requests.get(0).apiKey);
        assertEquals("Bearer user-access-token", requests.get(0).authorization);
    }

    @Test
    void workspaceLoadsAndUpsertsThroughRlsProtectedPostgrest() throws Exception {
        WorkspaceData loaded = client.loadWorkspace(session());
        assertTrue(loaded.actualDataImported);

        WorkspaceData toSave = new WorkspaceData();
        toSave.actualDataImported = true;
        client.saveWorkspace(session(), toSave);

        Request load = requests.get(0);
        Request save = requests.get(1);
        assertEquals("GET", load.method);
        assertEquals("/rest/v1/umkm_workspaces", load.path);
        assertEquals("Bearer user-access-token", load.authorization);
        assertEquals("POST", save.method);
        assertTrue(save.query.contains("on_conflict=user_id"));
        assertEquals("resolution=merge-duplicates,return=minimal", save.prefer);
        JsonObject savedRow = JsonParser.parseString(save.body).getAsJsonObject();
        assertEquals(USER_ID, savedRow.get("user_id").getAsString());
        assertTrue(savedRow.getAsJsonObject("payload").get("actualDataImported").getAsBoolean());
    }

    @Test
    void accountOperationsUseAuthenticatedSupabaseRpcFunctions() throws Exception {
        client.updateOwnName(session(), "Pemilik Usaha");
        client.setUserActive(session(), USER_ID, false);
        client.deleteUser(session(), USER_ID);
        client.deleteOwnAccount(session());

        assertEquals("/rest/v1/rpc/update_own_profile", requests.get(0).path);
        assertEquals("/rest/v1/rpc/admin_set_user_active", requests.get(1).path);
        assertEquals("/rest/v1/rpc/admin_delete_user", requests.get(2).path);
        assertEquals("/rest/v1/rpc/delete_own_account", requests.get(3).path);
        for (Request request : requests) {
            assertEquals("Bearer user-access-token", request.authorization);
            assertEquals("test-publishable-key", request.apiKey);
        }
    }

    @Test
    void signedInUserCanChangePasswordThroughSupabaseAuth() throws Exception {
        client.changePassword(session(), "StrongPassword123!");

        Request request = requests.get(0);
        assertEquals("PUT", request.method);
        assertEquals("/auth/v1/user", request.path);
        assertEquals("test-publishable-key", request.apiKey);
        assertEquals("Bearer user-access-token", request.authorization);
        assertEquals("StrongPassword123!",
            JsonParser.parseString(request.body).getAsJsonObject()
                .get("password").getAsString());
    }

    @Test
    void passwordResetAndSignupVerificationUseSupabaseAuthEndpoints() throws Exception {
        client.sendPasswordReset("pemilik@example.id");
        client.resendSignupVerification("pemilik@example.id");

        Request reset = requests.get(0);
        assertEquals("POST", reset.method);
        assertEquals("/auth/v1/recover", reset.path);
        assertEquals("test-publishable-key", reset.apiKey);
        assertEquals("pemilik@example.id",
            JsonParser.parseString(reset.body).getAsJsonObject().get("email").getAsString());

        Request resend = requests.get(1);
        assertEquals("POST", resend.method);
        assertEquals("/auth/v1/resend", resend.path);
        JsonObject resendBody = JsonParser.parseString(resend.body).getAsJsonObject();
        assertEquals("signup", resendBody.get("type").getAsString());
        assertEquals("pemilik@example.id", resendBody.get("email").getAsString());
    }

    @Test
    void signupRequestsOtpFromSupabaseWithoutHandlingOtpInJava() throws Exception {
        AuthSession session = client.signUp("Pemilik Usaha", "pemilik@example.id",
            "StrongPassword123!");

        assertEquals(null, session);
        Request request = requests.get(0);
        assertEquals("POST", request.method);
        assertEquals("/auth/v1/signup", request.path);
        assertEquals("test-publishable-key", request.apiKey);
        assertEquals(null, request.authorization);
        JsonObject body = JsonParser.parseString(request.body).getAsJsonObject();
        assertEquals("pemilik@example.id", body.get("email").getAsString());
        assertEquals("StrongPassword123!", body.get("password").getAsString());
        assertEquals("Pemilik Usaha", body.getAsJsonObject("data").get("nama").getAsString());
        assertEquals("application/json; charset=UTF-8", request.contentType);
    }

    @Test
    void signupAndRecoveryCodesAreVerifiedUsingSupabaseOtpEndpoint() throws Exception {
        client.verifyEmailOtp("pemilik@example.id", "123456", "signup");
        client.verifyEmailOtp("pemilik@example.id", "654321", "recovery");

        assertEquals(2, requests.size());
        assertOtpRequest(requests.get(0), "123456", "signup");
        assertOtpRequest(requests.get(1), "654321", "recovery");
    }

    private void assertOtpRequest(Request request, String token, String type) {
        assertEquals("POST", request.method);
        assertEquals("/auth/v1/verify", request.path);
        assertEquals("test-publishable-key", request.apiKey);
        JsonObject body = JsonParser.parseString(request.body).getAsJsonObject();
        assertEquals("pemilik@example.id", body.get("email").getAsString());
        assertEquals(token, body.get("token").getAsString());
        assertEquals(type, body.get("type").getAsString());
    }

    private void handle(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        requests.add(new Request(exchange, body));
        String path = exchange.getRequestURI().getPath();
        if ("/auth/v1/signup".equals(path)) {
            respond(exchange, 200, "{\"user\":{\"id\":\"" + USER_ID
                + "\",\"email\":\"pemilik@example.id\"},\"session\":null}");
        } else if ("/auth/v1/token".equals(path)) {
            respond(exchange, 400, "{\"code\":\"invalid_credentials\","
                + "\"message\":\"Invalid login credentials\"}");
        } else if ("/auth/v1/verify".equals(path)) {
            respond(exchange, 200, "{\"access_token\":\"verified-access-token\","
                + "\"refresh_token\":\"verified-refresh-token\",\"expires_in\":3600,"
                + "\"user\":{\"id\":\"" + USER_ID
                + "\",\"email\":\"pemilik@example.id\","
                + "\"email_confirmed_at\":\"2026-01-01T00:00:00Z\"}}");
        } else if ("/rest/v1/users".equals(path)) {
            respond(exchange, 200, "[{\"id\":\"" + USER_ID
                + "\",\"email\":\"pemilik@example.id\",\"nama\":\"Usaha Kopi\","
                + "\"role\":\"umkm\",\"is_active\":true,"
                + "\"last_active_at\":null,\"created_at\":\"2026-01-01T00:00:00Z\","
                + "\"verified_at\":\"2026-01-01T00:00:00Z\"}]");
        } else if ("/rest/v1/umkm_workspaces".equals(path)
                && "GET".equals(exchange.getRequestMethod())) {
            respond(exchange, 200, "[{\"payload\":{\"actualDataImported\":true}}]");
        } else {
            respond(exchange, 204, "");
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = exchange.getRequestBody().read(buffer)) != -1) {
            bytes.write(buffer, 0, count);
        }
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        if (status == 204) {
            exchange.sendResponseHeaders(status, -1);
        } else {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
        }
        exchange.close();
    }

    private AuthSession session() {
        AuthSession session = new AuthSession();
        session.userId = USER_ID;
        session.accessToken = "user-access-token";
        session.refreshToken = "user-refresh-token";
        session.expiresAt = System.currentTimeMillis() + 3600000;
        return session;
    }

    private static class Request {
        final String method;
        final String path;
        final String query;
        final String body;
        final String apiKey;
        final String contentType;
        final String authorization;
        final String prefer;

        Request(HttpExchange exchange, String body) {
            method = exchange.getRequestMethod();
            path = exchange.getRequestURI().getPath();
            query = exchange.getRequestURI().getRawQuery();
            this.body = body;
            apiKey = exchange.getRequestHeaders().getFirst("apikey");
            contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            authorization = exchange.getRequestHeaders().getFirst("Authorization");
            prefer = exchange.getRequestHeaders().getFirst("Prefer");
        }
    }
}
