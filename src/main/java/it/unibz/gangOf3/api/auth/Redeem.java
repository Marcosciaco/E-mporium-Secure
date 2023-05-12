package it.unibz.gangOf3.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.util.DatabaseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static it.unibz.gangOf3.util.BodyParser.parseBody;

public class Redeem extends HttpServlet {

    /**
     * Redeem a token (either registration or forgot password)
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectNode bodyJson = parseBody(req, resp, new String[]{"token", "type"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try {
            String type = bodyJson.get("type").asText();
            switch (type) {
                case "forgot":
                    if (!bodyJson.has("password")) {
                        resp.setStatus(400);
                        resp.getWriter().write("{\"status\": \"error\", \"message\": \"Missing required fields\"}");
                        return;
                    }
                    DatabaseUtil.getConnection()
                        .prepareStatement("UPDATE users SET password = '" + bodyJson.get("password").asText() + "', forgotToken = NULL WHERE forgotToken = '" + bodyJson.get("token").asText() + "';")
                        .execute();
                    response.set("status", mapper.valueToTree("ok"));
                    break;
                case "activate":
                    DatabaseUtil.getConnection()
                        .prepareStatement("UPDATE users SET registrationToken = NULL WHERE registrationToken = '" + bodyJson.get("token").asText() + "';")
                        .execute();
                    response.set("status", mapper.valueToTree("ok"));
                    break;
                default:
                    resp.setStatus(400);
                    response.set("status", mapper.valueToTree("error"));
                    response.set("message", mapper.valueToTree("Invalid type"));
            }
        } catch (Exception e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(response.toString());
    }
}
