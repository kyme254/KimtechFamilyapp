package com.kimtech.familysafety;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String WEBSITE =
            "https://kimtech.co.ke/parentguard/";

    private LinearLayout root;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildScreen();
        updatePermissionStatus();
        requestNeededPermissions();
    }

    private void buildScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(245, 247, 251));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(35), dp(24), dp(24));

        scrollView.addView(root);
        setContentView(scrollView);

        TextView title = new TextView(this);
        title.setText("Kimtech Family Safety");
        title.setTextSize(27);
        title.setTextColor(Color.rgb(20, 42, 75));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);
        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "Family protection with transparency and consent."
        );
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(12), 0, dp(25));
        root.addView(subtitle, matchWrap());

        addHeading("Device permissions");

        statusText = new TextView(this);
        statusText.setTextSize(15);
        statusText.setTextColor(Color.rgb(45, 55, 72));
        statusText.setPadding(dp(14), dp(14), dp(14), dp(14));
        statusText.setBackgroundColor(Color.WHITE);
        root.addView(statusText, matchWrap());

        Button permissionButton = makeButton("Review permissions");
        permissionButton.setOnClickListener(v -> {
            requestNeededPermissions();
            updatePermissionStatus();
        });
        addButton(permissionButton);

        addHeading("ParentGuard");

        TextView explanation = new TextView(this);
        explanation.setText(
                "Open the ParentGuard website to manage your account " +
                "and follow the setup instructions. Website access alone " +
                "does not mean this device has been registered or synced."
        );
        explanation.setTextSize(14);
        explanation.setTextColor(Color.DKGRAY);
        explanation.setPadding(0, dp(8), 0, dp(18));
        root.addView(explanation, matchWrap());

        Button websiteButton = makeButton("OPEN PARENTGUARD WEBSITE");
        websiteButton.setOnClickListener(v -> openWebsite());
        addButton(websiteButton);

        TextView footer = new TextView(this);
        footer.setText(
                "Kimtech Family Safety\n" +
                "Use only on devices you own or are authorized to manage."
        );
        footer.setTextSize(12);
        footer.setTextColor(Color.GRAY);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(30), 0, dp(10));
        root.addView(footer, matchWrap());
    }

    private void addHeading(String text) {
        TextView heading = new TextView(this);
        heading.setText(text);
        heading.setTextSize(18);
        heading.setTypeface(null, 1);
        heading.setTextColor(Color.rgb(20, 42, 75));
        heading.setPadding(0, dp(20), 0, dp(10));
        root.addView(heading, matchWrap());
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        return button;
    }

    private void addButton(Button button) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(12);
        root.addView(button, params);
    }

    private void updatePermissionStatus() {
        StringBuilder status = new StringBuilder();

        status.append("Internet: available to the app\n");

        boolean locationGranted =
                checkSelfPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        status.append("Location permission: ")
                .append(locationGranted ? "Granted" : "Not granted")
                .append("\n");

        if (Build.VERSION.SDK_INT >= 33) {
            boolean notificationsGranted =
                    checkSelfPermission(
                            Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED;

            status.append("Notifications: ")
                    .append(
                            notificationsGranted
                                    ? "Granted"
                                    : "Not granted"
                    );
        } else {
            status.append("Notifications: managed by Android");
        }

        if (statusText != null) {
            statusText.setText(status.toString());
        }
    }

    private void requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean locationMissing =
                    checkSelfPermission(
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED;

            if (locationMissing) {
                if (Build.VERSION.SDK_INT >= 33) {
                    requestPermissions(
                            new String[]{
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.POST_NOTIFICATIONS
                            },
                            100
                    );
                } else {
                    requestPermissions(
                            new String[]{
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            100
                    );
                }
                return;
            }

            if (Build.VERSION.SDK_INT >= 33
                    && checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        101
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );
        updatePermissionStatus();
    }

    private void openWebsite() {
        try {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(WEBSITE)
            );
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Unable to open ParentGuard website.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density
        );
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }
}
