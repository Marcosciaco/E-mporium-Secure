package it.unibz.gangOf3.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.repositories.UserRepository;
import it.unibz.gangOf3.util.ResponsePreprocessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static it.unibz.gangOf3.util.BodyParser.parseBody;

public class Forgot extends HttpServlet {

    /**
     * Send an email to the user with a link to reset the password
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"email"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try {
            User user = UserRepository.getUserByEmail(bodyJson.get("email").asText("").trim());
            user.forgotPassword();
            response.set("status", mapper.valueToTree("ok"));
        }catch (Exception e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(response.toString());
    }
}
