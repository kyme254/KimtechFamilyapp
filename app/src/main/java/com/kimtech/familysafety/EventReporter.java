package com.kimtech.familysafety;

import android.content.Context;

import org.json.JSONObject;

public final class EventReporter {

    private EventReporter() {
        // Utility class.
    }

    /**
     * Reports a basic app diagnostic event.
     *
     * The server's event.php endpoint must support this JSON format.
     * This method does not transmit SMS, calls, messages, or contacts.
     */
    public static void reportAppEvent(
            Context context,
            String eventName,
            String details,
            ApiClient.Callback callback
    ) {
        if (context == null ||
                eventName == null ||
                !eventName.matches("[A-Za-z0-9_-]{1,60}")) {

            if (callback != null) {
                callback.onError("Invalid app event.");
            }
            return;
        }

        JSONObject payload = new JSONObject();

        try {
            payload.put("event_type", eventName);
            payload.put(
                    "details",
                    details == null ? "" : details
            );
            payload.put("app", "Kimtech Family Safety");

        } catch (Exception e) {
            if (callback != null) {
                callback.onError(
                        "Could not prepare event report."
                );
            }
            return;
        }

        String token = SyncManager.getDeviceToken(context);

        ApiClient.postJson(
                "event.php",
                payload,
                token.isEmpty() ? null : token,
                callback
        );
    }
}
