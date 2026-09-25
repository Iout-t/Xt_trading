package com.xttrading.signals;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.app.Activity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private EditText backend;
    private Spinner pair;
    private TextView output;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        backend = new EditText(this);
        backend.setHint("Backend HTTPS URL");
        backend.setText(getSharedPreferences("signals", MODE_PRIVATE).getString("backend", BuildConfig.SIGNAL_BACKEND_URL));
        pair = new Spinner(this);
        pair.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"EUR/USD", "GBP/USD", "USD/JPY"}));
        output = new TextView(this);
        output.setTextSize(18);
        output.setPadding(0, 24, 0, 24);
        output.setText("No signal yet. The backend runs the V2.3 strategy.");
        Button start = new Button(this);
        start.setText("Start background signal checks");
        start.setOnClickListener(v -> startPolling());
        Button check = new Button(this);
        check.setText("Check now");
        check.setOnClickListener(v -> enqueueNow());
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        root.addView(new TextView(this) {{ setText("Forex Signals"); setTextSize(28); }});
        root.addView(new TextView(this) {{ setText("Signal-only • no orders"); }});
        root.addView(backend);
        root.addView(pair);
        root.addView(start);
        root.addView(check);
        root.addView(output);
        setContentView(root);
        observeResult();
    }

    private void saveSettings() {
        getSharedPreferences("signals", MODE_PRIVATE).edit().putString("backend", backend.getText().toString().trim()).apply();
        getSharedPreferences("signals", MODE_PRIVATE).edit().putString("pair", pair.getSelectedItem().toString()).apply();
    }

    private Constraints networkConstraints() {
        return new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    }

    private void enqueueNow() {
        saveSettings();
        WorkManager.getInstance(this).enqueueUniqueWork("signal-now", ExistingWorkPolicy.REPLACE,
                new OneTimeWorkRequest.Builder(SignalWorker.class).setConstraints(networkConstraints()).build());
    }

    private void startPolling() {
        saveSettings();
        enqueueNow();
        PeriodicWorkRequest periodic = new PeriodicWorkRequest.Builder(SignalWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(networkConstraints()).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("signal-poll", ExistingPeriodicWorkPolicy.UPDATE, periodic);
        output.setText("Background checks enabled. Android schedules them at least every 15 minutes.");
    }

    private void observeResult() {
        getSharedPreferences("signals", MODE_PRIVATE).registerOnSharedPreferenceChangeListener((prefs, key) -> {
            if ("last_result".equals(key)) output.setText(prefs.getString(key, "No signal yet."));
        });
    }
}
