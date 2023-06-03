package it.unibz.gangOf3.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.util.DatabaseUtil;
import it.unibz.gangOf3.util.ResponsePreprocessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Logout extends HttpServlet {

    /**
     * Logout the user by setting the sessionToken to null
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);

        String sessionID = req.getHeader("Authorization");

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("UPDATE users SET sessionToken = NULL WHERE sessionToken = ?;");
            stmt.setString(1, sessionID);
            stmt.execute();
            response.set("status", mapper.valueToTree("ok"));
        } catch (SQLException e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }
    }
}
