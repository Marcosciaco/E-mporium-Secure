package it.unibz.gangOf3.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.exceptions.UsernameTakenException;
import it.unibz.gangOf3.model.repositories.UserRepository;
import it.unibz.gangOf3.util.ResponsePreprocessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Pattern;

import static it.unibz.gangOf3.util.BodyParser.parseBody;

public class Register extends HttpServlet {

    /**
     * Register a new user
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"username", "email", "password", "type"});

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        //create user
        try {
            UserRepository.createUser(
                bodyJson.get("username").asText("").trim(),
                bodyJson.get("email").asText("").trim(),
                bodyJson.get("password").asText("").trim(),
                bodyJson.get("type").asText("").trim(),
                bodyJson.has("emergencyEmail") ? bodyJson.get("emergencyEmail").asText("").trim() : null,
                bodyJson.has("emergencyPhone") ? bodyJson.get("emergencyPhone").asText("").trim() : null
            );
            response.set("status", mapper.valueToTree("ok"));

        } catch (Exception e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }
}
