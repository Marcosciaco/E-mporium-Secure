package it.unibz.gangOf3.api.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.repositories.ProductRepository;
import it.unibz.gangOf3.model.repositories.UserRepository;
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
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"name", "tag", "description", "price", "category"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        String name = sanitize(bodyJson.get("name").asText("").trim());
        String tag = sanitize(bodyJson.get("tag").asText("").trim());
        String description = sanitize(bodyJson.get("description").asText("").trim());
        double price = bodyJson.get("price").asDouble(1.00);
        String category = sanitize(bodyJson.get("category").asText("").trim());
        int stock = bodyJson.get("stock").asInt();
        String image = sanitize(bodyJson.get("image").asText("").trim());

        try{
            int productID = ProductRepository.createProduct(
                user,
                name,
                tag,
                description,
                price,
                category,
                stock,
                image
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
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"filter", "fields", "max"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        ObjectNode filter = (ObjectNode) bodyJson.get("filter");
        ArrayNode fields = (ArrayNode) bodyJson.get("fields");
        int max = bodyJson.get("max").asInt(1);

        LinkedList<it.unibz.gangOf3.model.classes.Product> queryResult = new LinkedList<>();

        //Get product by filter

        if(filter.has("random")) {
            try {
                ProductRepository.getRandomProducts(queryResult, max);
            } catch (SQLException | NotFoundException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

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
            } catch (SQLException | NotFoundException e) {
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
            } catch (SQLException | NotFoundException e) {
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
            } catch (SQLException | NotFoundException e) {
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


        ArrayNode data = mapper.createArrayNode();
        for (it.unibz.gangOf3.model.classes.Product product : queryResult) {
            try {
                data.add(product.getAsJSON(fields, mapper));
            } catch (Exception e) {
                // ignore me
            }
        }
        response.set("status", mapper.valueToTree("ok"));
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
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"id"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        int id = bodyJson.get("id").asInt();

        try{
            it.unibz.gangOf3.model.classes.Product product = ProductRepository.getProductById(id);
            if (!product.getOwner().equals(user)) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree("You are not the owner of this product"));
                resp.getWriter().write(response.toString());
                return;
            }
            product.delete();
            response.set("status", mapper.valueToTree("ok"));
        } catch (SQLException | NotFoundException e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }
}
