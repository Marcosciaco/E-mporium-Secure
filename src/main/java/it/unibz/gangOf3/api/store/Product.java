package it.unibz.gangOf3.api.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.User;
import it.unibz.gangOf3.model.utils.ProductUtil;
import it.unibz.gangOf3.util.AuthUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static it.unibz.gangOf3.util.BodyParser.parseBody;

public class Product extends HttpServlet {

    /**
     * Create a new product
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectNode bodyJson = parseBody(req, resp, new String[]{"name", "tag", "description", "price", "category"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try{
            ProductUtil.createProduct(
                user,
                bodyJson.get("name").asText(),
                bodyJson.get("tag").asText(),
                bodyJson.get("description").asText(),
                bodyJson.get("price").asDouble(),
                bodyJson.get("category").asText()
            );
        }catch (Exception e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(response.toString());
    }

    /**
     * Get product(s)
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Get product(s)
    }

    /**
     * Delete a product
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Delete product
    }
}
