package com.smarthealth.util;

import java.util.List;

/** Symptom vocabulary shared with the ML service (ml-service/app.py). Only these values are accepted. */
public final class Symptoms {
    public static final List<String> ALL = List.of(
        "fever", "cough", "fatigue", "body ache", "headache", "sore throat", "runny nose", "sneezing",
        "loss of taste", "shortness of breath", "wheezing", "chest pain", "nausea", "vomiting", "diarrhea",
        "abdominal pain", "light sensitivity", "dizziness", "skin rash", "itching", "joint pain");

    /** Hourly slots with a lunch break. */
    public static final List<String> SLOTS = List.of("09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00");
    private Symptoms() {}
}
