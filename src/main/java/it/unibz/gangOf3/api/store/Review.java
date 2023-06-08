package it.unibz.gangOf3.api.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.repositories.ReviewRepository;
import it.unibz.gangOf3.util.AuthUtil;
import it.unibz.gangOf3.util.ResponsePreprocessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

import static it.unibz.gangOf3.util.BodyParser.parseBody;
import static it.unibz.gangOf3.util.security.CSRFHandler.handleCSRF;
import static it.unibz.gangOf3.util.security.XSSSanitizer.sanitize;

public class Review extends HttpServlet {

    /**
     * Get review(s)
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"product"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try{
            LinkedList<it.unibz.gangOf3.model.classes.Review> reviews = ReviewRepository.getReviewsForProduct(bodyJson.get("product").asInt());
            response.set("status", mapper.valueToTree("ok"));
            ArrayNode data = mapper.createArrayNode();
            response.set("data", data);
            for (it.unibz.gangOf3.model.classes.Review review : reviews) {
                ObjectNode reviewJson = review.toJSON(mapper);
                data.add(reviewJson);
            }
        }catch (Exception e) {
            e.printStackTrace();
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }

    /**
     * Create a new review
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"message", "product", "stars"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try{
            ReviewRepository.createReview(
                user,
                bodyJson.get("product").asInt(),
                bodyJson.get("stars").asInt(5),
                sanitize(bodyJson.get("message").asText("").trim())
            );
            response.set("status", mapper.valueToTree("ok"));
        }catch (Exception ex) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(ex.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }

    /**
     * Delete a review
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"id"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try{
            it.unibz.gangOf3.model.classes.Review review = ReviewRepository.getReviewById(bodyJson.get("id").asInt());
            if (!review.getWriter().equals(user)) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree("You are not the creator of this review"));
                resp.getWriter().write(response.toString());
                return;
            }
            review.delete();
            response.set("status", mapper.valueToTree("ok"));
        } catch (SQLException | NotFoundException e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }
}
