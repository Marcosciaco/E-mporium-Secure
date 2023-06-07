package it.unibz.gangOf3.api.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.repositories.ChatRepository;
import it.unibz.gangOf3.model.repositories.UserRepository;
import it.unibz.gangOf3.util.AuthUtil;
import it.unibz.gangOf3.util.ResponsePreprocessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static it.unibz.gangOf3.util.BodyParser.parseBody;
import static it.unibz.gangOf3.util.security.CSRFHandler.handleCSRF;

public class Key extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"partner"}); //max per sender - receiver - couple
        if (bodyJson == null) return; // parseBody already sent the response (400)

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // authorize already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try {
            User partner = UserRepository.getUserByUsername(bodyJson.get("partner").asText("").trim());
            String key = ChatRepository.generateSymmetricKey(user, partner);

            response.set("status", mapper.valueToTree("ok"));
            ObjectNode data = mapper.createObjectNode();
            data.set("key", mapper.valueToTree(key));
            response.set("data", data);
        }catch (Exception e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }
}
