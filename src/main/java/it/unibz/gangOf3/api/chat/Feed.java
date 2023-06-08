package it.unibz.gangOf3.api.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.classes.Message;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.repositories.UserRepository;
import it.unibz.gangOf3.util.ResponsePreprocessor;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import static it.unibz.gangOf3.util.security.CSRFHandler.handleCSRF;

@WebServlet(urlPatterns = "/api/chat/feed", asyncSupported = true)
public class Feed extends HttpServlet {

    private static final ConcurrentHashMap<Integer, AsyncContext> feededUsers = new ConcurrentHashMap<>();

    /**
     * "Server Sent Events" endpoint for incoming chat messages
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.addHeader("connection", "keep-alive");

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        User user = null;
        try {
            user = UserRepository.getUserBySessionId(req.getParameter("token"));
        } catch (SQLException | NotFoundException e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
            resp.getWriter().write(mapper.writeValueAsString(response));
            return;
        }

        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(0);

        if (user != null) {
            try {
                feededUsers.put(user.getID(), asyncContext);
            } catch (SQLException | NotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        resp.flushBuffer();
    }

    /**
     * Send a "server sent event" to asynchronously feed incoming messages to the user
     *
     * @param message message to feed
     */
    public static void notify(Message message) {
        User receiver = null;
        try {
            receiver = message.getTo();
        } catch (SQLException | NotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            if (feededUsers.containsKey(receiver.getID())) {
                AsyncContext asyncContext = feededUsers.get(receiver.getID());
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    PrintWriter writer = asyncContext.getResponse().getWriter();
                    writer.write("data: " + mapper.writeValueAsString(message.getAsJson(mapper)) + "\n\n");
                    writer.flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (SQLException | NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
