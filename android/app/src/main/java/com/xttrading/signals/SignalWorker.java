package com.xttrading.signals;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignalWorker extends Worker {
    public SignalWorker(@NonNull Context context, @NonNull WorkerParameters params) { super(context, params); }

    @NonNull @Override public Result doWork() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("signals", Context.MODE_PRIVATE);
        String base = prefs.getString("backend", BuildConfig.SIGNAL_BACKEND_URL).replaceAll("/$", "");
        String pair = prefs.getString("pair", "EUR/USD");
        if (base.contains("YOUR-BACKEND")) {
            prefs.edit().putString("last_result", "Configure the remote backend HTTPS URL first.").apply();
            return Result.failure();
        }
        try {
            String body = post(base + "/signal/live", "{\"pair\":\"" + pair + "\"}");
            String status = value(body, "status");
            String reason = value(body, "reason");
            String side = value(body, "side");
            String probability = value(body, "probability");
            String result = status + "\n" + pair + (side.isEmpty() ? "" : " • " + side)
                    + (probability.isEmpty() ? "" : "\nProbability: " + probability)
                    + (reason.isEmpty() ? "" : "\n" + reason)
                    + "\nUpdated: now";
            prefs.edit().putString("last_result", result).apply();
            return Result.success();
        } catch (Exception e) {
            prefs.edit().putString("last_result", "Backend unavailable\n" + e.getMessage()).apply();
            return Result.retry();
        }
    }

    private String post(String endpoint, String payload) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST"); conn.setConnectTimeout(15000); conn.setReadTimeout(30000);
        conn.setRequestProperty("Content-Type", "application/json"); conn.setDoOutput(true);
        try (OutputStream out = conn.getOutputStream()) { out.write(payload.getBytes(StandardCharsets.UTF_8)); }
        InputStream stream = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line; while ((line = reader.readLine()) != null) body.append(line);
        }
        if (conn.getResponseCode() >= 400) throw new Exception(body.toString());
        return body.toString();
    }

    private String value(String json, String key) {
        Matcher m = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*(?:\\\"([^\\\"]*)\\\"|([0-9.]+))").matcher(json);
        return m.find() ? (m.group(1) != null ? m.group(1) : m.group(2)) : "";
    }
}
