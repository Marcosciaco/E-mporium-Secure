package it.unibz.gangOf3.util;

import jakarta.servlet.http.HttpServletResponse;

public class ResponsePreprocessor {

    public static void preprocessResponse(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    }

}
