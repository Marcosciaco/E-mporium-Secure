package it.unibz.gangOf3.util;

import java.util.HashMap;
import java.util.Map;

public class QueryParser {

    public static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            result.put(keyValue[0], keyValue[1]);
        }
        return result;
    }

}
