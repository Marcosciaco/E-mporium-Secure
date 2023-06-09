package it.unibz.gangOf3.api.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotAuthorizedException;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.repositories.MessageRepository;
import it.unibz.gangOf3.model.repositories.UserRepository;
import it.unibz.gangOf3.util.AuthUtil;
import it.unibz.gangOf3.util.ResponsePreprocessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;

import static it.unibz.gangOf3.util.BodyParser.parseBody;
import static it.unibz.gangOf3.util.security.CSRFHandler.handleCSRF;
import static it.unibz.gangOf3.util.security.XSSSanitizer.sanitize;

public class Message extends HttpServlet {

    /**
     * Fetch already existing messages
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"filter", "max"}); //max per sender - receiver - couple
        if (bodyJson == null) return; // parseBody already sent the response (400)

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // authorize already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        ObjectNode filter = (ObjectNode) bodyJson.get("filter");
        int max = bodyJson.get("max").asInt(1);
        LinkedList<it.unibz.gangOf3.model.classes.Message> queryResult = new LinkedList<>();

        //Get Messages by filter

        if (filter.has("id")) {
            try {
                it.unibz.gangOf3.model.classes.Message msg = MessageRepository.getMessageById(filter.get("id").asInt());
                if (!msg.getFrom().equals(user) && !msg.getTo().equals(user))
                    throw new NotAuthorizedException("You are not authorized to access this message");
                queryResult.add(msg);
            } catch (SQLException | NotFoundException | NotAuthorizedException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        if (filter.has("since")) {
            try {
                Timestamp timestamp = Timestamp.valueOf(filter.get("since").asText());
                MessageRepository.filterBySince(user, timestamp, queryResult, max);
            } catch (Exception ex) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(ex.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        if (filter.has("user2")) {
            try {
                User u2 = UserRepository.getUserByEmail(filter.get("user2").asText());
                MessageRepository.filterByUsers(user, u2, queryResult, max);
            } catch (SQLException | NotFoundException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        if (filter.has("latest")) {
            try {
                MessageRepository.filterByLatest(queryResult, max);
            } catch (SQLException ex) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(ex.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        ArrayNode data = mapper.createArrayNode();
        for (it.unibz.gangOf3.model.classes.Message message : queryResult) {
            try {
                data.add(message.getAsJson(mapper));
            } catch (Exception e) {
                //Ignore me
            }
        }
        response.set("status", mapper.valueToTree("ok"));
        response.set("data", data);

        resp.getWriter().write(mapper.writeValueAsString(response));
    }

    /**
     * Send a message
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"to", "message"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try {
            //Create message
            User to = UserRepository.getUserByEmail(bodyJson.get("to").asText("").trim());
            int messageID = MessageRepository.createMessage(
                user,
                to,
                sanitize(bodyJson.get("message").asText("").trim())
            );
            response.set("status", mapper.valueToTree("ok"));
            ObjectNode data = mapper.createObjectNode();
            response.set("data", data);
            data.set("messageID", mapper.valueToTree(messageID));

            //Notify receiver of message
            it.unibz.gangOf3.model.classes.Message messageInstance = MessageRepository.getMessageById(messageID);
            Feed.notify(messageInstance);
        } catch (Exception ex) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(ex.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }

    /**
     * Get all chat partners of a user
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try {
            LinkedList<Integer> chatPartners = MessageRepository.getChatPartners(user);
            ArrayNode data = mapper.createArrayNode();
            for (int chatPartnerId : chatPartners) {
                try {
                    data.add(UserRepository.getUserById(chatPartnerId).getUsername());
                } catch (NotFoundException e) {
                    //Partner does not exist anymore, ignore
                }
            }
            response.set("status", mapper.valueToTree("ok"));
            response.set("data", data);
        } catch (SQLException | NotFoundException ex) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(ex.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }
}
