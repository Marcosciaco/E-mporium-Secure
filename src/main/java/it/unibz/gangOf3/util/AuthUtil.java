package it.unibz.gangOf3.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.User;
import it.unibz.gangOf3.model.exceptions.UserNotFoundException;
import it.unibz.gangOf3.model.utils.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.print.attribute.standard.JobKOctets;
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
            user = UserUtil.getUserBySessionId(sessionID);
        } catch (SQLException | UserNotFoundException e) {
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
