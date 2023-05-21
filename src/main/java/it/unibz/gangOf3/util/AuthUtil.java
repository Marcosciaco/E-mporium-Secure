package it.unibz.gangOf3.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.SQLException;

public class AuthUtil {

    public static User getAuthedUser(HttpServletRequest req, HttpServletResponse response) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode res = mapper.createObjectNode();
        res.set("status", mapper.valueToTree("error"));
        //Get header with sessionID
        String sessionID = req.getHeader("Authorization");
        if (sessionID == null){
            res.set("message", mapper.valueToTree("Not authorized"));
            try {
                response.setStatus(401);
                response.getWriter().write(mapper.writeValueAsString(res));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        User user = null;
        try {
            user = UserRepository.getUserBySessionId(sessionID);
        } catch (SQLException | NotFoundException e) {
            res.set("message", mapper.valueToTree(e.getMessage()));
            try {
                response.setStatus(401);
                response.getWriter().write(mapper.writeValueAsString(res));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return user;
    }

}
