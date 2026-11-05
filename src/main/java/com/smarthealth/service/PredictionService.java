package com.smarthealth.service;

import com.google.gson.Gson;
import com.smarthealth.util.Config;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Calls the ML disease-prediction REST API (POST /predict, JSON in/out). */
public final class PredictionService {
    private static final Logger LOG = Logger.getLogger(PredictionService.class.getName());
    private static final Gson GSON = new Gson();
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    public static class Prediction {
        public String disease;
        public double confidence;
        public String specialist;
    }

    private PredictionService() {}

    /** Returns null on timeout, connection error, or a bad response so the caller can show a fallback. */
    public static Prediction predict(List<String> symptoms) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(Config.get("ml.api.url", "http://localhost:5000/predict")))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(Map.of("symptoms", symptoms))))
                    .build();
            HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                LOG.warning("ML service returned HTTP " + resp.statusCode());
                return null;
            }
            Prediction p = GSON.fromJson(resp.body(), Prediction.class);
            return (p != null && p.disease != null && !p.disease.isBlank()) ? p : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "ML service unavailable: " + e.getMessage());
            return null;
        }
    }
}
