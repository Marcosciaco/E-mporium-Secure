package it.unibz.gangOf3.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
            String password = bodyJson.get("password").asText().trim();
            Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$");
            if (!pattern.matcher(password).matches()) {
                throw new Exception("Password must be at least 8 characters long and contain at least one digit, one lowercase and one uppercase letter");
            }

            UserRepository.createUser(
                bodyJson.get("username").asText(),
                bodyJson.get("email").asText(),
                password,
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
