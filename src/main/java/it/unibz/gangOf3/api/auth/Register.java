package it.unibz.gangOf3.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.User;
import it.unibz.gangOf3.model.exceptions.UserAlreadyExistsException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

import static it.unibz.gangOf3.util.BodyParser.parseBody;

public class Register extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //get body of request
        byte[] body = req.getInputStream().readAllBytes();
        String bodyStr = new String(body);
        JsonNode bodyJson = parseBody(bodyStr);
        if (bodyJson == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid body\"}");
            return;
        }

        //check if required fields are present
        if (!bodyJson.has("username")
            || !bodyJson.has("email")
            || !bodyJson.has("password")
            || !bodyJson.has("type")) {
            resp.setStatus(400);
            resp.getWriter().write("{\"status\": \"error\", \"message\": \"Missing required fields\"}");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        //create user
        try {
            User.createUser(
                bodyJson.get("username").asText(),
                bodyJson.get("email").asText(),
                bodyJson.get("password").asText(),
                bodyJson.get("type").asText(),
                bodyJson.has("emergencyEmail") ? bodyJson.get("emergencyEmail").asText() : null,
                bodyJson.has("emergencyPhone") ? bodyJson.get("emergencyPhone").asText() : null
            );
            response.set("status", mapper.valueToTree("ok"));

        } catch (Exception e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }
}
