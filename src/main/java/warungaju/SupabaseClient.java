package warungaju;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SupabaseClient {
    private static final String DEFAULT_URL = "https://sisbkliywimhdygtimfx.supabase.co";
    private static final String DEFAULT_PUBLISHABLE_KEY = "sb_publishable_GXvWJfY75haUsaN7QEmVpg_o3Ux8x9a";
    private static final String DEFAULT_API_URL = "http://127.0.0.1:8081";
    private static final int DEFAULT_TIMEOUT_MS = 12000;
    private static final int SIGN_UP_TIMEOUT_MS = 60000;
    private static final Gson GSON = new Gson();
    private final String baseUrl;
    private final String apiUrl;
    private final String publishableKey;

    public SupabaseClient() {
        String configuredUrl = System.getenv("SUPABASE_URL");
        String configuredKey = System.getenv("SUPABASE_PUBLISHABLE_KEY");
        String configuredApiUrl = System.getenv("WARUNGAJU_API_URL");
        baseUrl = (configuredUrl == null || configuredUrl.trim().isEmpty()
            ? DEFAULT_URL : configuredUrl.trim()).replaceAll("/+$", "");
        apiUrl = (configuredApiUrl == null || configuredApiUrl.trim().isEmpty()
            ? DEFAULT_API_URL : configuredApiUrl.trim()).replaceAll("/+$", "");
        publishableKey = configuredKey == null || configuredKey.trim().isEmpty()
            ? DEFAULT_PUBLISHABLE_KEY : configuredKey.trim();
    }

    public AuthSession signIn(String email, String password) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email.trim());
        body.addProperty("password", password);
        return sessionFrom(send("POST", "/auth/v1/token?grant_type=password",
            GSON.toJson(body), null));
    }

    public AuthSession signUp(String fullName, String email, String password) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email.trim());
        body.addProperty("password", password);
        JsonObject metadata = new JsonObject();
        metadata.addProperty("nama", fullName.trim());
        body.add("data", metadata);
        String response;
        try {
            response = send("POST", "/auth/v1/signup", GSON.toJson(body), null, null,
                SIGN_UP_TIMEOUT_MS, SIGN_UP_TIMEOUT_MS);
        } catch (SocketTimeoutException ex) {
            throw new IOException(
                "Supabase melewati batas waktu saat mendaftar. Akun mungkin sudah dibuat "
                    + "tetapi respons/email konfirmasi terlambat. Periksa Authentication > Users "
                    + "dan folder spam sebelum mencoba daftar lagi. Periksa juga SMTP dan Auth Logs.",
                ex);
        }
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return json.has("access_token") && !json.get("access_token").isJsonNull()
            && !json.get("access_token").getAsString().trim().isEmpty()
            ? sessionFrom(response) : null;
    }

    public void signOut(AuthSession session) throws IOException {
        send("POST", "/auth/v1/logout", "{}", session);
    }

    public AppUser getProfile(AuthSession session) throws IOException {
        return GSON.fromJson(sendApi("GET", "/api/profile", null, session), AppUser.class);
    }

    public List<AppUser> getActiveProfiles(AuthSession session) throws IOException {
        return profilesFrom(sendApi("GET", "/api/profiles?active=true", null, session));
    }

    public List<AppUser> getAllProfiles(AuthSession session) throws IOException {
        return profilesFrom(sendApi("GET", "/api/profiles", null, session));
    }

    private List<AppUser> profilesFrom(String response) {
        JsonArray users = JsonParser.parseString(response).getAsJsonArray();
        List<AppUser> result = new ArrayList<>();
        for (JsonElement user : users) {
            result.add(GSON.fromJson(user, AppUser.class));
        }
        return result;
    }

    // --- Update / Delete akun. Database operations run in the JDBC backend. ---

    public void updateOwnName(AuthSession session, String fullName) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("p_nama", fullName.trim());
        sendApi("POST", "/api/account/update-name", GSON.toJson(body), session);
    }

    public void changePassword(AuthSession session, String newPassword) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("password", newPassword);
        send("PUT", "/auth/v1/user", GSON.toJson(body), session);
    }

    public void deleteOwnAccount(AuthSession session) throws IOException {
        sendApi("POST", "/api/account/delete-own", "{}", session);
    }

    public void setUserActive(AuthSession session, String userId, boolean active)
            throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("p_user_id", userId);
        body.addProperty("p_active", active);
        sendApi("POST", "/api/account/set-active", GSON.toJson(body), session);
    }

    public void deleteUser(AuthSession session, String userId) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("p_user_id", userId);
        sendApi("POST", "/api/account/delete-user", GSON.toJson(body), session);
    }

    public WorkspaceData loadWorkspace(AuthSession session) throws IOException {
        WorkspaceData data = GSON.fromJson(
            sendApi("GET", "/api/workspace", null, session), WorkspaceData.class);
        if (data == null) data = new WorkspaceData();
        data.normalize();
        return data;
    }

    public void saveWorkspace(AuthSession session, WorkspaceData data) throws IOException {
        sendApi("PUT", "/api/workspace", GSON.toJson(data), session);
    }

    private AuthSession sessionFrom(String response) throws IOException {
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonObject user = json.has("user") && json.get("user").isJsonObject()
            ? json.getAsJsonObject("user") : json;
        AuthSession session = new AuthSession();
        session.accessToken = stringValue(json, "access_token");
        session.refreshToken = stringValue(json, "refresh_token");
        session.userId = stringValue(user, "id");
        session.email = stringValue(user, "email");
        long lifetime = json.has("expires_in") ? json.get("expires_in").getAsLong() : 3600;
        session.expiresAt = System.currentTimeMillis() + lifetime * 1000L;
        if (session.accessToken == null || session.userId == null) {
            throw new IOException("Supabase tidak mengembalikan sesi login yang valid.");
        }
        return session;
    }

    private void refreshIfNeeded(AuthSession session) throws IOException {
        if (session == null || session.refreshToken == null
                || session.expiresAt > System.currentTimeMillis() + 60000L) {
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("refresh_token", session.refreshToken);
        AuthSession refreshed = sessionFrom(send("POST",
            "/auth/v1/token?grant_type=refresh_token", GSON.toJson(body), null));
        session.accessToken = refreshed.accessToken;
        session.refreshToken = refreshed.refreshToken;
        session.expiresAt = refreshed.expiresAt;
    }

    private String send(String method, String path, String body, AuthSession session)
            throws IOException {
        return send(method, path, body, session, null);
    }

    private String send(String method, String path, String body, AuthSession session,
                        String prefer) throws IOException {
        return send(method, path, body, session, prefer,
            DEFAULT_TIMEOUT_MS, DEFAULT_TIMEOUT_MS);
    }

    private String send(String method, String path, String body, AuthSession session,
                        String prefer, int connectTimeoutMs, int readTimeoutMs)
            throws IOException {
        refreshIfNeeded(session);
        HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(connectTimeoutMs);
            connection.setReadTimeout(readTimeoutMs);
            connection.setRequestProperty("apikey", publishableKey);
            if (session != null) {
                connection.setRequestProperty("Authorization", "Bearer " + session.accessToken);
            }
            connection.setRequestProperty("Accept", "application/json");
            if (prefer != null) connection.setRequestProperty("Prefer", prefer);
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(bytes);
                }
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream() : connection.getErrorStream();
            String response = stream == null ? "" : read(stream);
            if (status < 200 || status >= 300) {
                throw new IOException(errorMessage(response, status));
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }

    private String sendApi(String method, String path, String body, AuthSession session)
            throws IOException {
        refreshIfNeeded(session);
        URL endpoint = new URL(apiUrl + path);
        String protocol = endpoint.getProtocol();
        String host = endpoint.getHost();
        boolean localHttp = "http".equalsIgnoreCase(protocol)
            && ("127.0.0.1".equalsIgnoreCase(host) || "localhost".equalsIgnoreCase(host)
                || "[::1]".equalsIgnoreCase(host));
        if (!"https".equalsIgnoreCase(protocol) && !localHttp) {
            throw new IOException("WARUNGAJU_API_URL harus menggunakan HTTPS "
                + "(HTTP hanya diizinkan untuk backend lokal).");
        }
        HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
            connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
            connection.setRequestProperty("Authorization", "Bearer " + session.accessToken);
            connection.setRequestProperty("Accept", "application/json");
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(bytes);
                }
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream() : connection.getErrorStream();
            String response = stream == null ? "" : read(stream);
            if (status < 200 || status >= 300) {
                throw new IOException(errorMessage(response, status));
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }

    private String read(InputStream stream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }

    private String errorMessage(String response, int status) {
        String detail = null;
        if (response != null && !response.trim().isEmpty()) {
            try {
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                for (String key : new String[]{"msg", "message", "error_description", "error"}) {
                    if (json.has(key) && !json.get(key).isJsonNull()) {
                        detail = json.get(key).getAsString();
                        break;
                    }
                }
            } catch (IllegalStateException | com.google.gson.JsonSyntaxException ex) {
                detail = response;
            }
        }
        if (status == HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {
            return "Supabase mengembalikan gateway timeout (504). Permintaan daftar mungkin "
                + "sudah diproses; cek Authentication > Users sebelum mencoba lagi. Periksa "
                + "juga Auth Logs dan konfigurasi SMTP. Detail: "
                + (detail == null ? "gateway timeout" : detail);
        }
        if (detail != null) {
            return "Supabase mengembalikan HTTP " + status + ": " + detail;
        }
        return "Permintaan Supabase gagal (HTTP " + status + ").";
    }

    private String stringValue(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull()
            ? json.get(key).getAsString() : null;
    }
}
