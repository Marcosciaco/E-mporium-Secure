package it.unibz.gangOf3.util;

import jakarta.servlet.http.HttpServletResponse;

public class ResponsePreprocessor {

    public static void preprocessResponse(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "https://127.0.0.1:8443"); // Allow CORS
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Allow CORS
        response.addHeader("X-FRAME-OPTIONS", "DENY"); // Prevent clickjacking
    }

}
