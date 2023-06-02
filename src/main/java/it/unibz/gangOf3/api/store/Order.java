package it.unibz.gangOf3.api.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.classes.Product;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.repositories.OrderRepository;
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

public class Order extends HttpServlet {

    /**
     * Get order(s)
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"filter", "fields"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        ObjectNode filter = (ObjectNode) bodyJson.get("filter");
        ArrayNode fields = (ArrayNode) bodyJson.get("fields");

        LinkedList<it.unibz.gangOf3.model.classes.Order> queryResult = new LinkedList<>();

        if (filter.has("id")) {
            try {
                queryResult.add(OrderRepository.getOrderById(filter.get("id").asInt()));
            } catch (Exception ex) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(ex.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        if (filter.has("buyer")) {
            try{
                User buyer = UserRepository.getUserByEmail(filter.get("buyer").asText());
                OrderRepository.filterOrdersByBuyer(buyer, queryResult);
            } catch (SQLException | NotFoundException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        if (filter.has("product")) {
            try{
                Product product = ProductRepository.getProductById(filter.get("product").asInt());
                OrderRepository.filterOrdersByProduct(product, queryResult);
            } catch (SQLException | NotFoundException e) {
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }
        }

        ArrayNode data = mapper.createArrayNode();
        for (it.unibz.gangOf3.model.classes.Order order : queryResult) {
            try {
                data.add(order.getAsJSON(fields, mapper));
            } catch (Exception e) {
                //Ignore me
            }
        }
        response.set("status", mapper.valueToTree("ok"));
        response.set("data", data);

        resp.getWriter().write(mapper.writeValueAsString(response));
    }


    /**
     * Create a new order
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);

        ObjectNode bodyJson = parseBody(req, resp, new String[]{"product", "quantity"});
        if (bodyJson == null) return; // parseBody already sent the response (400)

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try{
            Product toBeOrdered = ProductRepository.getProductById(bodyJson.get("product").asInt());
            int orderID = OrderRepository.createOrder(
                user,
                toBeOrdered,
                bodyJson.get("quantity").asInt()
            );
            response.set("status", mapper.valueToTree("ok"));
            ObjectNode data = mapper.createObjectNode();
            response.set("data", data);
            data.set("orderID", mapper.valueToTree(orderID));
        } catch (SQLException | NotFoundException e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }
}
