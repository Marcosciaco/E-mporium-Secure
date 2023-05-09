package it.unibz.gangOf3.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class BodyParser {

    public static ObjectNode parseBody(HttpServletRequest req, HttpServletResponse resp, String[] requiredFields) {
        byte[] body;
        try {
            body = req.getInputStream().readAllBytes();
        } catch (IOException e) {
            resp.setStatus(400);
            try {
                resp.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid body\"}");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return null;
        }
        String bodyStr = new String(body);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode reqJson;
        try {
            reqJson = (ObjectNode) objectMapper.readTree(body);
        } catch (Exception e) {
            resp.setStatus(400);
            try {
                resp.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid body\"}");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return null;
        }

        //check if required fields are present
        for (String field : requiredFields) {
            if (!reqJson.has(field)) {
                resp.setStatus(400);
                try {
                    resp.getWriter().write("{\"status\": \"error\", \"message\": \"Missing required fields\"}");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                return null;
            }
        }

        return reqJson;
    }

}
