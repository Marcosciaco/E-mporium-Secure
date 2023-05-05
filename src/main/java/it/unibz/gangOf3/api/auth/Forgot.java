package it.unibz.gangOf3.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static it.unibz.gangOf3.util.BodyParser.parseBody;

public class Forgot extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        byte[] body = req.getInputStream().readAllBytes();
        String bodyStr = new String(body);
        JsonNode bodyJson = parseBody(bodyStr);
        if (bodyJson == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid body\"}");
            return;
        }

        //check if required fields are present
        if (!bodyJson.has("email")) {
            resp.setStatus(400);
            resp.getWriter().write("{\"status\": \"error\", \"message\": \"Missing required fields\"}");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try {
            User user = User.getUser(bodyJson.get("email").asText());
            user.forgotPassword();
            response.set("status", mapper.valueToTree("ok"));
        }catch (Exception e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(response.toString());
    }
}
