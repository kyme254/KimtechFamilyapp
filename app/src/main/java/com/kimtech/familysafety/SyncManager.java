package com.kimtech.familysafety;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.json.JSONObject;

public final class SyncManager {

    private static final String PREFS_NAME =
            "kimtech_sync_settings";

    private static final String KEY_DEVICE_TOKEN =
            "device_token";

    private SyncManager() {
        // Utility class.
    }

    public interface ConnectionCallback {
        void onResult(boolean connected, String message);
    }

    /**
     * Save a device token obtained from the ParentGuard server.
     * Do not invent a token locally; the server must issue it.
     */
    public static void saveDeviceToken(
            Context context,
            String deviceToken
    ) {
        if (context == null ||
                deviceToken == null ||
                deviceToken.trim().isEmpty()) {
            return;
        }

        SharedPreferences preferences =
                context.getApplicationContext()
                        .getSharedPreferences(
                                PREFS_NAME,
                                Context.MODE_PRIVATE
                        );

        preferences.edit()
                .putString(
                        KEY_DEVICE_TOKEN,
                        deviceToken.trim()
                )
                .apply();
    }

    public static String getDeviceToken(Context context) {
        SharedPreferences preferences =
                context.getApplicationContext()
                        .getSharedPreferences(
                                PREFS_NAME,
                                Context.MODE_PRIVATE
                        );

        return preferences.getString(
                KEY_DEVICE_TOKEN,
                ""
        );
    }

    public static void clearDeviceToken(Context context) {
        SharedPreferences preferences =
                context.getApplicationContext()
                        .getSharedPreferences(
                                PREFS_NAME,
                                Context.MODE_PRIVATE
                        );

        preferences.edit()
                .remove(KEY_DEVICE_TOKEN)
                .apply();
    }

    /**
     * Checks the API only if the server's auth.php supports
     * this JSON POST action. A successful GET request to auth.php
     * does not prove that this POST request is supported.
     */
    public static void checkConnection(
            Context context,
            ConnectionCallback callback
    ) {
        JSONObject payload = new JSONObject();

        try {
            payload.put("action", "connection_check");
            payload.put("app", "Kimtech Family Safety");
            payload.put("version", getAppVersion(context));
        } catch (Exception e) {
            if (callback != null) {
                callback.onResult(
                        false,
                        "Could not prepare connection request."
                );
            }
            return;
        }

        String token = getDeviceToken(context);

        ApiClient.postJson(
                "auth.php",
                payload,
                token.isEmpty() ? null : token,
                new ApiClient.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        if (callback != null) {
                            callback.onResult(
                                    true,
                                    "ParentGuard accepted the connection request."
                            );
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (callback != null) {
                            callback.onResult(false, message);
                        }
                    }
                }
        );
    }

    private static String getAppVersion(Context context) {
        try {
            PackageInfo info =
                    context.getPackageManager()
                            .getPackageInfo(
                                    context.getPackageName(),
                                    0
                            );

            return info.versionName == null
                    ? "unknown"
                    : info.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }
}
