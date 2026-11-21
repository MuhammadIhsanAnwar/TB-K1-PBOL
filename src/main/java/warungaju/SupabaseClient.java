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
    private static final String PROFILE_COLUMNS =
        "id,email,nama,role,is_active,last_active_at,created_at,verified_at";
    private static final int DEFAULT_TIMEOUT_MS = 12000;
    private static final int SIGN_UP_TIMEOUT_MS = 60000;
    private static final Gson GSON = new Gson();
    private final String baseUrl;
    private final String publishableKey;

    public SupabaseClient() {
        this(DEFAULT_URL, DEFAULT_PUBLISHABLE_KEY);
    }

    SupabaseClient(String baseUrl, String publishableKey) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.publishableKey = publishableKey;
    }

    ConnectionCheck checkConnection() throws IOException {
        HttpURLConnection connection =
            (HttpURLConnection) new URL(baseUrl + "/auth/v1/health").openConnection();
        long startedAt = System.nanoTime();
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("apikey", publishableKey);
            connection.setRequestProperty("Accept", "application/json");
            int status = connection.getResponseCode();
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L;
            return new ConnectionCheck(status, elapsedMillis);
        } finally {
            connection.disconnect();
        }
    }

    static final class ConnectionCheck {
        final int httpStatus;
        final long elapsedMillis;

        ConnectionCheck(int httpStatus, long elapsedMillis) {
            this.httpStatus = httpStatus;
            this.elapsedMillis = elapsedMillis;
        }
    }

    public AuthSession signIn(String email, String password) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email.trim());
        body.addProperty("password", password);
        AuthSession session = sessionFrom(send("POST", "/auth/v1/token?grant_type=password",
            GSON.toJson(body), null));
        if (!session.emailVerified) {
            throw new IOException("Verifikasi email terlebih dahulu sebelum masuk.");
        }
        return session;
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
        if (json.has("access_token") && !json.get("access_token").isJsonNull()
            && !json.get("access_token").getAsString().trim().isEmpty()
        ) {
            AuthSession session = sessionFrom(response);
            return session.emailVerified ? session : null;
        }
        return null;
    }

    public void sendPasswordReset(String email) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email.trim());
        send("POST", "/auth/v1/recover", GSON.toJson(body), null);
    }

    public AuthSession verifyEmailOtp(String email, String token, String type)
            throws IOException {
        if (!"signup".equals(type) && !"recovery".equals(type)) {
            throw new IOException("Jenis verifikasi email tidak valid.");
        }
        JsonObject body = new JsonObject();
        body.addProperty("email", email.trim());
        body.addProperty("token", token.trim());
        body.addProperty("type", type);
        return sessionFrom(send("POST", "/auth/v1/verify", GSON.toJson(body), null));
    }

    public void resendSignupVerification(String email) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("type", "signup");
        body.addProperty("email", email.trim());
        send("POST", "/auth/v1/resend", GSON.toJson(body), null);
    }

    public void signOut(AuthSession session) throws IOException {
        send("POST", "/auth/v1/logout", "{}", session);
    }

    public AppUser getProfile(AuthSession session) throws IOException {
        requireSession(session);
        String path = "/rest/v1/users?id=eq." + encodeQueryValue(session.userId)
            + "&select=" + PROFILE_COLUMNS;
        JsonArray rows = arrayFrom(send("GET", path, null, session));
        if (rows.size() == 0) {
            throw new IOException("Profil akun tidak ditemukan atau tidak dapat diakses.");
        }
        return appUserFrom(rows.get(0));
    }

    public List<AppUser> getActiveProfiles(AuthSession session) throws IOException {
        requireSession(session);
        String path = "/rest/v1/users?is_active=eq.true&select=" + PROFILE_COLUMNS
            + "&order=created_at.desc";
        return profilesFrom(send("GET", path, null, session));
    }

    public List<AppUser> getAllProfiles(AuthSession session) throws IOException {
        requireSession(session);
        String path = "/rest/v1/users?select=" + PROFILE_COLUMNS + "&order=created_at.desc";
        return profilesFrom(send("GET", path, null, session));
    }

    private List<AppUser> profilesFrom(String response) throws IOException {
        JsonArray users = arrayFrom(response);
        List<AppUser> result = new ArrayList<>();
        for (JsonElement user : users) {
            result.add(appUserFrom(user));
        }
        return result;
    }

    private JsonArray arrayFrom(String response) throws IOException {
        JsonElement parsed = JsonParser.parseString(response);
        if (!parsed.isJsonArray()) {
            throw new IOException("Respons Supabase tidak berisi daftar data yang valid.");
        }
        return parsed.getAsJsonArray();
    }

    private AppUser appUserFrom(JsonElement element) throws IOException {
        if (!element.isJsonObject()) {
            throw new IOException("Data profil dari Supabase tidak valid.");
        }
        JsonObject json = element.getAsJsonObject();
        AppUser user = new AppUser();
        user.id = stringValue(json, "id");
        user.email = stringValue(json, "email");
        user.fullName = stringValue(json, "nama");
        user.role = stringValue(json, "role");
        user.active = json.has("is_active") && !json.get("is_active").isJsonNull()
            && json.get("is_active").getAsBoolean();
        user.lastActiveAt = stringValue(json, "last_active_at");
        user.createdAt = stringValue(json, "created_at");
        user.verifiedAt = stringValue(json, "verified_at");
        return user;
    }

    // --- Account operations use Supabase PostgREST RPC with the signed-in user's JWT. ---

    public void updateOwnName(AuthSession session, String fullName) throws IOException {
        requireSession(session);
        JsonObject body = new JsonObject();
        body.addProperty("p_nama", fullName.trim());
        send("POST", "/rest/v1/rpc/update_own_profile", GSON.toJson(body), session);
    }

    public void changePassword(AuthSession session, String newPassword) throws IOException {
        requireSession(session);
        JsonObject body = new JsonObject();
        body.addProperty("password", newPassword);
        send("PUT", "/auth/v1/user", GSON.toJson(body), session);
    }

    public void deleteOwnAccount(AuthSession session) throws IOException {
        requireSession(session);
        send("POST", "/rest/v1/rpc/delete_own_account", "{}", session);
    }

    public void setUserActive(AuthSession session, String userId, boolean active)
            throws IOException {
        requireSession(session);
        JsonObject body = new JsonObject();
        body.addProperty("p_user_id", userId);
        body.addProperty("p_active", active);
        send("POST", "/rest/v1/rpc/admin_set_user_active", GSON.toJson(body), session);
    }

    public void deleteUser(AuthSession session, String userId) throws IOException {
        requireSession(session);
        JsonObject body = new JsonObject();
        body.addProperty("p_user_id", userId);
        send("POST", "/rest/v1/rpc/admin_delete_user", GSON.toJson(body), session);
    }

    public WorkspaceData loadWorkspace(AuthSession session) throws IOException {
        requireSession(session);
        String response = send("GET",
            "/rest/v1/umkm_workspaces?select=payload&limit=1", null, session);
        JsonArray rows = arrayFrom(response);
        WorkspaceData data = new WorkspaceData();
        if (!rows.isEmpty()) {
            JsonElement row = rows.get(0);
            if (!row.isJsonObject()) {
                throw new IOException("Baris workspace dari Supabase tidak valid.");
            }
            JsonElement payload = row.getAsJsonObject().get("payload");
            if (payload == null || payload.isJsonNull() || !payload.isJsonObject()) {
                throw new IOException("Payload workspace dari Supabase tidak valid.");
            }
            data = GSON.fromJson(payload, WorkspaceData.class);
        }
        if (data == null) data = new WorkspaceData();
        data.normalize();
        return data;
    }

    public void saveWorkspace(AuthSession session, WorkspaceData data) throws IOException {
        requireSession(session);
        if (data == null) {
            throw new IOException("Data workspace tidak boleh kosong.");
        }
        JsonObject row = new JsonObject();
        row.addProperty("user_id", session.userId);
        row.add("payload", GSON.toJsonTree(data));
        send("POST", "/rest/v1/umkm_workspaces?on_conflict=user_id",
            GSON.toJson(row), session, "resolution=merge-duplicates,return=minimal");
    }

    private void requireSession(AuthSession session) throws IOException {
        if (session == null || session.accessToken == null || session.userId == null) {
            throw new IOException("Sesi Supabase tidak valid. Silakan masuk kembali.");
        }
    }

    private String encodeQueryValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException("UTF-8 tidak tersedia.", ex);
        }
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
        session.emailVerified = stringValue(user, "email_confirmed_at") != null;
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
