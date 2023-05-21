package it.unibz.gangOf3.api.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.repositories.MessageRepository;
import it.unibz.gangOf3.model.repositories.UserRepository;
import it.unibz.gangOf3.util.AuthUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;

import static it.unibz.gangOf3.util.BodyParser.parseBody;

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
        ObjectNode bodyJson = parseBody(req, resp, new String[]{"filter", "max"}); //max per sender - receiver - couple
        if (bodyJson == null) return; // parseBody already sent the response (400)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        ObjectNode filter = (ObjectNode) bodyJson.get("filter");
        int max = bodyJson.get("max").asInt();
        LinkedList<it.unibz.gangOf3.model.classes.Message> queryResult = new LinkedList<>();

        //Get Messages by filter

        if (filter.has("id")) {
            try{
                queryResult.add(MessageRepository.getMessageById(filter.get("id").asInt()));
            } catch (SQLException | NotFoundException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        if (filter.has("since")) {
            try {
                Timestamp timestamp = Timestamp.valueOf(filter.get("since").asText());
                MessageRepository.filterBySince(timestamp, queryResult, max);
            } catch (Exception ex) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(ex.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        if (filter.has("sender")) {
            try {
                User sender = UserRepository.getUserById(filter.get("sender").asInt());
                MessageRepository.filterBySender(sender, queryResult, max);
            } catch (SQLException | NotFoundException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        if (filter.has("receiver")) {
            try {
                User receiver = UserRepository.getUserById(filter.get("receiver").asInt());
                MessageRepository.filterByReceiver(receiver, queryResult, max);
            } catch (SQLException | NotFoundException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        if (filter.has("latest")) { //FIXME move to bottom
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
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectNode bodyJson = parseBody(req, resp, new String[]{"to", "message"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try {
            //Create message
            User to = UserRepository.getUserByEmail(bodyJson.get("to").asText());
            int messageID = MessageRepository.createMessage(
                user,
                to,
                bodyJson.get("message").asText()
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
}
