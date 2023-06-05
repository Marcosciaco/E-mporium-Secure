package it.unibz.gangOf3.util.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.UUID;

public class CSRFHandler {

    private static final String CSRF_COOKIE_NAME = "CSRF-TOKEN";
    private static HashMap<String, String> csrfTokens = new HashMap<>();

    /**
     * Check and update CSRF token
     * @param req The request to check
     * @param resp The response where the cookie will be set
     * @return true if the CSRF token is valid, false otherwise
     */
    public static boolean handleCSRF(HttpServletRequest req, HttpServletResponse resp) {
        try {
            // Check CSRF token
            String sessionId = req.getHeader("Authorization");
            if (sessionId == null) throw new Exception("No session id");
            Cookie[] cookies = req.getCookies();
            if (cookies == null) throw new Exception("No cookies");
            String sessionid = null;
            String csrfToken = null;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CSRF_COOKIE_NAME)) {
                    csrfToken = cookie.getValue();
                }
            }
            if (csrfToken == null) throw new Exception("No CSRF token");
            if (!csrfTokens.containsKey(sessionId)) throw new Exception("No CSRF token");
            if (!csrfTokens.get(sessionId).equals(csrfToken)) throw new Exception("Invalid CSRF token");

            // Update CSRF token
            UUID uuid = UUID.randomUUID();
            csrfTokens.put(sessionId, uuid.toString());
            Cookie cookie = new Cookie(CSRF_COOKIE_NAME, uuid.toString());
            cookie.setPath("/api");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            resp.addCookie(cookie);
            return true;
        }catch (Exception e){
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode res = mapper.createObjectNode();
            res.set("status", mapper.valueToTree("error"));
            res.set("message", mapper.valueToTree(e.getMessage()));
            try {
                resp.setStatus(402);
                resp.getWriter().write(mapper.writeValueAsString(res));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }


    /**
     * Initially set a CSRF token
     * @param sessionid The session id
     * @param resp The response where the cookie will be set
     */
    public static void initCSRF(String sessionid, HttpServletResponse resp) {
        UUID uuid = UUID.randomUUID();
        csrfTokens.put(sessionid, uuid.toString());
        Cookie cookie = new Cookie(CSRF_COOKIE_NAME, uuid.toString());
        cookie.setPath("/api");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        resp.addCookie(cookie);
    }

    public static void endCSRF(String sessionid, HttpServletResponse resp) {
        csrfTokens.remove(sessionid);
        Cookie cookie = new Cookie(CSRF_COOKIE_NAME, "");
        cookie.setPath("/api");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
    }

}
