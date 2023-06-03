package it.unibz.gangOf3.api.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibz.gangOf3.model.classes.Message;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.AuthUtil;
import it.unibz.gangOf3.util.ResponsePreprocessor;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet(urlPatterns = "/api/chat/feed", asyncSupported = true)
public class Feed extends HttpServlet {

    private static final ConcurrentHashMap<Integer, AsyncContext> feededUsers = new ConcurrentHashMap<>();

    /**
     * "Server Sent Events" endpoint for incoming chat messages
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);

        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.addHeader("connection", "keep-alive");

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(0);

        try {
            feededUsers.put(user.getID(), asyncContext);
        } catch (SQLException | NotFoundException e) {
            throw new RuntimeException(e);
        }
        resp.flushBuffer();
    }

    /**
     * Send a "server sent event" to asynchronously feed incoming messages to the user
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
            System.out.println("Notifying user: " + receiver.getUsername() + " about message: " + message.getMessage());
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
