package warungaju;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

public class SupabaseJdbcServer {
    private static final Gson GSON = new Gson();
    private static final int REQUEST_TIMEOUT_MS = 10000;
    private static final int MAX_REQUEST_BYTES = 2 * 1024 * 1024;
    private static final String DEFAULT_SUPABASE_URL =
        "https://sisbkliywimhdygtimfx.supabase.co";
    private static final String DEFAULT_PUBLISHABLE_KEY =
        "sb_publishable_GXvWJfY75haUsaN7QEmVpg_o3Ux8x9a";

    private final String supabaseUrl;
    private final String publishableKey;
    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;
    private final String bindAddress;
    private final int port;

    public SupabaseJdbcServer() throws IOException, ClassNotFoundException {
        supabaseUrl = configuredValue("SUPABASE_URL", DEFAULT_SUPABASE_URL);
        publishableKey = configuredValue("SUPABASE_PUBLISHABLE_KEY",
            DEFAULT_PUBLISHABLE_KEY);
        databaseUrl = requiredEnvironment("WARUNGAJU_DB_URL");
        databaseUser = requiredEnvironment("WARUNGAJU_DB_USER");
        databasePassword = requiredEnvironment("WARUNGAJU_DB_PASSWORD");
        bindAddress = configuredValue("WARUNGAJU_API_BIND", "127.0.0.1");
        try {
            port = Integer.parseInt(configuredValue("WARUNGAJU_API_PORT", "8081"));
        } catch (NumberFormatException ex) {
            throw new IOException("WARUNGAJU_API_PORT harus berupa angka.", ex);
        }
        if (!databaseUrl.startsWith("jdbc:postgresql://")) {
            throw new IOException("WARUNGAJU_DB_URL harus menggunakan driver PostgreSQL JDBC.");
        }
        if (!databaseUrl.toLowerCase().contains("sslmode=require")
                && !databaseUrl.toLowerCase().contains("sslmode=verify-full")) {
            throw new IOException("WARUNGAJU_DB_URL harus mengaktifkan sslmode=require "
                + "atau sslmode=verify-full.");
        }
        Class.forName("org.postgresql.Driver");
    }

    public static void main(String[] args) throws Exception {
        SupabaseJdbcServer service = new SupabaseJdbcServer();
        service.start();
    }

    private void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(bindAddress, port), 0);
        server.createContext("/api/", this::handle);
        server.setExecutor(Executors.newFixedThreadPool(8));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
        server.start();
        System.out.println("Warung Aju JDBC API listening on " + bindAddress + ":" + port);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            Object result = route(exchange);
            sendJson(exchange, 200, result);
        } catch (ApiException ex) {
            sendError(exchange, ex.status, ex.getMessage());
        } catch (JsonParseException | IllegalStateException | IllegalArgumentException ex) {
            sendError(exchange, 400, "Permintaan JSON tidak valid.");
        } catch (SQLException ex) {
            System.err.println("Supabase JDBC request failed: " + ex.getMessage());
            if ("P0001".equals(ex.getSQLState())) {
                sendError(exchange, 403, ex.getMessage());
                return;
            }
            sendError(exchange, 500, "Operasi database gagal.");
        } catch (IOException ex) {
            System.err.println("Warung Aju API request failed: " + ex.getMessage());
            sendError(exchange, 502, "Backend atau Supabase Auth tidak dapat dijangkau.");
        } finally {
            exchange.close();
        }
    }

    private Object route(HttpExchange exchange) throws IOException, SQLException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String accessToken = accessToken(exchange);
        if ("GET".equals(method) && "/api/profile".equals(path)) {
            return inAuthenticatedTransaction(accessToken, (connection, userId) -> {
                AppUser user = selectUser(connection, userId);
                if (user == null) throw new ApiException(404, "Profil akun tidak ditemukan.");
                return user;
            });
        }
        if ("GET".equals(method) && "/api/profiles".equals(path)) {
            boolean activeOnly = "active=true".equals(exchange.getRequestURI().getRawQuery());
            return inAuthenticatedTransaction(accessToken,
                (connection, userId) -> selectUsers(connection, activeOnly));
        }
        if ("GET".equals(method) && "/api/workspace".equals(path)) {
            return inAuthenticatedTransaction(accessToken, (connection, userId) -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "select payload::text from public.umkm_workspaces "
                            + "where user_id = ?::uuid")) {
                    statement.setString(1, userId);
                    try (ResultSet rows = statement.executeQuery()) {
                        if (!rows.next()) return new WorkspaceData();
                        WorkspaceData data = GSON.fromJson(rows.getString(1),
                            WorkspaceData.class);
                        return data == null ? new WorkspaceData() : data;
                    }
                }
            });
        }
        if ("PUT".equals(method) && "/api/workspace".equals(path)) {
            WorkspaceData data = GSON.fromJson(readJson(exchange), WorkspaceData.class);
            if (data == null) throw new ApiException(400, "Data workspace tidak valid.");
            String payload = GSON.toJson(data);
            inAuthenticatedTransaction(accessToken, (connection, userId) -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "insert into public.umkm_workspaces (user_id, payload) "
                            + "values (?::uuid, ?::jsonb) "
                            + "on conflict (user_id) do update set payload = excluded.payload")) {
                    statement.setString(1, userId);
                    statement.setString(2, payload);
                    statement.executeUpdate();
                    return null;
                }
            });
            return success();
        }
        if ("POST".equals(method) && path.startsWith("/api/account/")) {
            JsonObject body = readJson(exchange);
            return inAuthenticatedTransaction(accessToken, (connection, userId) -> {
                if ("/api/account/update-name".equals(path)) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "select public.update_own_profile(?)")) {
                        statement.setString(1, requiredString(body, "p_nama"));
                        statement.execute();
                    }
                } else if ("/api/account/delete-own".equals(path)) {
                    executeFunction(connection, "select public.delete_own_account()");
                } else if ("/api/account/set-active".equals(path)) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "select public.admin_set_user_active(?::uuid, ?::boolean)")) {
                        statement.setString(1, requiredUuid(body, "p_user_id"));
                        if (!body.has("p_active") || body.get("p_active").isJsonNull()) {
                            throw new ApiException(400, "Status akun tidak valid.");
                        }
                        statement.setBoolean(2, body.get("p_active").getAsBoolean());
                        statement.execute();
                    }
                } else if ("/api/account/delete-user".equals(path)) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "select public.admin_delete_user(?::uuid)")) {
                        statement.setString(1, requiredUuid(body, "p_user_id"));
                        statement.execute();
                    }
                } else {
                    throw new ApiException(404, "Endpoint tidak ditemukan.");
                }
                return null;
            });
        }
        throw new ApiException(404, "Endpoint tidak ditemukan.");
    }

    private interface AuthenticatedWork<T> {
        T run(Connection connection, String userId) throws SQLException, ApiException;
    }

    private <T> T inAuthenticatedTransaction(String accessToken, AuthenticatedWork<T> work)
            throws SQLException, IOException {
        String userId = validateAccessToken(accessToken);
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                setAuthenticatedClaims(connection, userId);
                T result = work.run(connection, userId);
                connection.commit();
                return result;
            } catch (SQLException | ApiException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    private AppUser selectUser(Connection connection, String profileId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "select id::text, email, nama, role, is_active, last_active_at::text, "
                    + "created_at::text, verified_at::text from public.users "
                    + "where id = ?::uuid")) {
            statement.setString(1, profileId);
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? appUserFrom(row) : null;
            }
        }
    }

    private List<AppUser> selectUsers(Connection connection, boolean activeOnly)
            throws SQLException {
        String sql = "select id::text, email, nama, role, is_active, last_active_at::text, "
            + "created_at::text, verified_at::text from public.users "
            + (activeOnly ? "where is_active " : "")
            + "order by created_at desc";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<AppUser> users = new ArrayList<>();
            while (rows.next()) users.add(appUserFrom(rows));
            return users;
        }
    }

    private AppUser appUserFrom(ResultSet row) throws SQLException {
        AppUser user = new AppUser();
        user.id = row.getString("id");
        user.email = row.getString("email");
        user.fullName = row.getString("nama");
        user.role = row.getString("role");
        user.active = row.getBoolean("is_active");
        user.lastActiveAt = row.getString("last_active_at");
        user.createdAt = row.getString("created_at");
        user.verifiedAt = row.getString("verified_at");
        return user;
    }

    private void setAuthenticatedClaims(Connection connection, String userId)
            throws SQLException {
        Map<String, String> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("role", "authenticated");
        claims.put("aud", "authenticated");
        try (PreparedStatement statement = connection.prepareStatement(
                "select set_config('request.jwt.claims', ?, true), "
                    + "set_config('request.jwt.claim.sub', ?, true), "
                    + "set_config('request.jwt.claim.role', 'authenticated', true)")) {
            statement.setString(1, GSON.toJson(claims));
            statement.setString(2, userId);
            statement.execute();
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("set local role authenticated");
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
    }

    private String validateAccessToken(String accessToken) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(
            supabaseUrl + "/auth/v1/user").openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(REQUEST_TIMEOUT_MS);
            connection.setReadTimeout(REQUEST_TIMEOUT_MS);
            connection.setRequestProperty("apikey", publishableKey);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream() : connection.getErrorStream();
            String response = stream == null ? "" : read(stream);
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED
                    || status == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new ApiException(401, "Sesi login tidak valid atau sudah kedaluwarsa.");
            }
            if (status < 200 || status >= 300) {
                throw new ApiException(502, "Supabase Auth gagal memvalidasi sesi (HTTP "
                    + status + ").");
            }
            JsonObject user = JsonParser.parseString(response).getAsJsonObject();
            String userId = requiredString(user, "id");
            try {
                return UUID.fromString(userId).toString();
            } catch (IllegalArgumentException ex) {
                throw new ApiException(401, "Supabase Auth mengembalikan identitas tidak valid.");
            }
        } finally {
            connection.disconnect();
        }
    }

    private String accessToken(HttpExchange exchange) throws ApiException {
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")
                || authorization.length() <= 7) {
            throw new ApiException(401, "Access token Supabase diperlukan.");
        }
        return authorization.substring(7).trim();
    }

    private JsonObject readJson(HttpExchange exchange) throws IOException, ApiException {
        byte[] bytes = readLimited(exchange.getRequestBody());
        JsonObject json = JsonParser.parseString(
            new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        return json;
    }

    private byte[] readLimited(InputStream input) throws IOException, ApiException {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int total = 0;
            int count;
            while ((count = stream.read(buffer)) != -1) {
                total += count;
                if (total > MAX_REQUEST_BYTES) {
                    throw new ApiException(413, "Ukuran permintaan terlalu besar.");
                }
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }

    private String requiredString(JsonObject json, String key) throws ApiException {
        if (!json.has(key) || json.get(key).isJsonNull()
                || !json.get(key).isJsonPrimitive()
                || !json.get(key).getAsJsonPrimitive().isString()
                || json.get(key).getAsString().trim().isEmpty()) {
            throw new ApiException(400, "Field " + key + " wajib diisi.");
        }
        return json.get(key).getAsString().trim();
    }

    private String requiredUuid(JsonObject json, String key) throws ApiException {
        try {
            return UUID.fromString(requiredString(json, key)).toString();
        } catch (IllegalArgumentException ex) {
            throw new ApiException(400, "Field " + key + " harus berupa UUID.");
        }
    }

    private void executeFunction(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private String read(InputStream input) throws IOException {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("error", message);
        sendJson(exchange, status, body);
    }

    private JsonObject success() {
        return new JsonObject();
    }

    private static String configuredValue(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static String requiredEnvironment(String name) throws IOException {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IOException("Environment variable " + name + " wajib diatur.");
        }
        return value.trim();
    }

    private static final class ApiException extends IOException {
        private final int status;

        private ApiException(int status, String message) {
            super(message);
            this.status = status;
        }
    }
}
