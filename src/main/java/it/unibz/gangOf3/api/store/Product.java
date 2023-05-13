package it.unibz.gangOf3.api.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.utils.ProductRepository;
import it.unibz.gangOf3.model.utils.UserRepository;
import it.unibz.gangOf3.util.AuthUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

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
            int productID = ProductRepository.createProduct(
                user,
                bodyJson.get("name").asText(),
                bodyJson.get("tag").asText(),
                bodyJson.get("description").asText(),
                bodyJson.get("price").asDouble(),
                bodyJson.get("category").asText()
            );
            response.set("status", mapper.valueToTree("ok"));
            ObjectNode data = mapper.createObjectNode();
            response.set("data", data);
            data.set("productID", mapper.valueToTree(productID));
        }catch (Exception e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(response.toString());
    }

    /**
     * Get product(s) based on query
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectNode bodyJson = parseBody(req, resp, new String[]{"filter", "fields", "max"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        ObjectNode filter = (ObjectNode) bodyJson.get("filter");
        ArrayNode fields = (ArrayNode) bodyJson.get("fields");
        int max = bodyJson.get("max").asInt();

        LinkedList<it.unibz.gangOf3.model.Product> queryResult = new LinkedList<>();

        if(filter.has("random")) {
            try {
                ProductRepository.getRandomProducts(queryResult, max);
            } catch (SQLException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        //Get product by filter
        if (filter.has("id")) {
            try {
                queryResult.add(ProductRepository.getProductById(filter.get("id").asInt()));
            } catch (Exception e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        //Get products by search term (name, tag, description)
        if (filter.has("query")) {
            try {
                ProductRepository.filterProductsByQuery(filter.get("query").asText(), queryResult, max);
            } catch (SQLException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        //Get products by category
        if (filter.has("category")) {
            try {
                ProductRepository.filterProductsByCategory(filter.get("category").asText(), queryResult, max);
            } catch (SQLException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        //Get products by price range
        if (filter.has("price")) {
            try {
                ProductRepository.filterProductsByPrice(filter.get("price").get("min").asDouble(), filter.get("price").get("max").asDouble(), queryResult, max);
            } catch (SQLException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        //Get products by seller
        if (filter.has("seller")) {
            try {
                User seller = UserRepository.getUserByEmail(filter.get("seller").asText());
                ProductRepository.filterProductsByOwner(seller, queryResult, max);
            } catch (SQLException | NotFoundException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        if (queryResult.size() == 0) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree("No products found"));
            resp.getWriter().write(mapper.writeValueAsString(response));
            return;
        }

        response.set("status", mapper.valueToTree("ok"));

        ArrayNode data = mapper.createArrayNode();
        for (it.unibz.gangOf3.model.Product product : queryResult) {
            data.add(product.getAsJSON(fields, mapper));
        }
        response.set("data", data);

        resp.getWriter().write(mapper.writeValueAsString(response));
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
        ObjectNode bodyJson = parseBody(req, resp, new String[]{"id"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        int id = bodyJson.get("id").asInt();

        try{
            it.unibz.gangOf3.model.Product product = ProductRepository.getProductById(id);
//            FIXME check if user is owner of product
//            if (product.getOwner().getID() != user.getID()) {
//                response.set("status", mapper.valueToTree("error"));
//                response.set("message", mapper.valueToTree("You are not the owner of this product"));
//                resp.getWriter().write(response.toString());
//                return;
//            }
            product.delete();
            response.set("status", mapper.valueToTree("ok"));
        } catch (SQLException | NotFoundException e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree("No products found"));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }
}
