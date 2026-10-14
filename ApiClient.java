package com.kimtech.familysafety;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ApiClient {

    public static final String BASE_URL =
            "https://kimtech.co.ke/parentguard/api/data/";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private ApiClient() {
        // Utility class.
    }

    public interface Callback {
        void onSuccess(JSONObject response);

        void onError(String message);
    }

    public static void postJson(
            String endpoint,
            JSONObject payload,
            String deviceToken,
            Callback callback
    ) {
        if (endpoint == null ||
                !endpoint.matches("[A-Za-z0-9_-]+\\.php")) {
            if (callback != null) {
                callback.onError("Invalid API endpoint.");
            }
            return;
        }

        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(BASE_URL + endpoint);

                connection = (HttpURLConnection)
                        url.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                if (deviceToken != null &&
                        !deviceToken.trim().isEmpty()) {
                    connection.setRequestProperty(
                            "Authorization",
                            "Bearer " + deviceToken.trim()
                    );
                }

                byte[] requestBytes =
                        payload.toString().getBytes(
                                StandardCharsets.UTF_8
                        );

                try (OutputStream output =
                             connection.getOutputStream()) {
                    output.write(requestBytes);
                    output.flush();
                }

                int statusCode =
                        connection.getResponseCode();

                InputStream stream =
                        statusCode >= 200 && statusCode < 400
                                ? connection.getInputStream()
                                : connection.getErrorStream();

                StringBuilder responseText =
                        new StringBuilder();

                if (stream != null) {
                    try (BufferedReader reader =
                                 new BufferedReader(
                                         new InputStreamReader(
                                                 stream,
                                                 StandardCharsets.UTF_8
                                         ))) {
                        String line;

                        while ((line = reader.readLine()) != null) {
                            responseText.append(line);
                        }
                    }
                }

                JSONObject response;

                try {
                    response = new JSONObject(
                            responseText.toString()
                    );
                } catch (Exception parseError) {
                    throw new Exception(
                            "Server returned an invalid JSON response. " +
                            "HTTP status: " + statusCode
                    );
                }

                boolean serverAccepted =
                        statusCode >= 200 &&
                        statusCode < 300 &&
                        response.optBoolean("ok", false);

                if (serverAccepted) {
                    deliverSuccess(callback, response);
                } else {
                    String message = response.optString(
                            "error",
                            "Request rejected. HTTP status: " +
                                    statusCode
                    );

                    deliverError(callback, message);
                }

            } catch (Exception e) {
                String message = e.getMessage();

                if (message == null || message.trim().isEmpty()) {
                    message = "Could not connect to ParentGuard.";
                }

                deliverError(callback, message);

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static void deliverSuccess(
            Callback callback,
            JSONObject response
    ) {
        if (callback == null) {
            return;
        }

        android.os.Handler handler =
                new android.os.Handler(
                        android.os.Looper.getMainLooper()
                );

        handler.post(() -> callback.onSuccess(response));
    }

    private static void deliverError(
            Callback callback,
            String message
    ) {
        if (callback == null) {
            return;
        }

        android.os.Handler handler =
                new android.os.Handler(
                        android.os.Looper.getMainLooper()
                );

        handler.post(() -> callback.onError(message));
    }
}
