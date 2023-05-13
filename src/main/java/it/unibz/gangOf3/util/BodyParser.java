package it.unibz.gangOf3.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class BodyParser {

    /**
     * Parse the body of a request and check if all the required fields are present
     * @param req the request
     * @param resp the response
     * @param requiredFields the required fields in the body to check for
     * @return the body of the request as a JSON object
     */
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
