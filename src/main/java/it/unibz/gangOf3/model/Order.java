package it.unibz.gangOf3.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class Order {

    private int id;

    public Order(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public User getBuyer() throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT buyer FROM orders WHERE id = " + id + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Order not found");
        return new User(resultSet.getInt("buyer"));
    }

    public User getSeller() throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT seller FROM orders WHERE id = " + id + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Order not found");
        return new User(resultSet.getInt("seller"));
    }

    public Product getProduct() throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT product FROM orders WHERE id = " + id + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Order not found");
        return new Product(resultSet.getInt("product"));
    }

    public int getQuantity() throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT quantity FROM orders WHERE id = " + id + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Order not found");
        return resultSet.getInt("quantity");
    }

    public ObjectNode getAsJSON(ArrayNode fields, ObjectMapper mapper) throws SQLException, NotFoundException {
        ObjectNode node = mapper.createObjectNode();
        for (Iterator<JsonNode> it = fields.elements(); it.hasNext();){
            JsonNode element = it.next();
            String fieldName = element.asText();
            switch (fieldName) {
                case "id" -> node.put(fieldName, id);
                case "buyer" -> node.put(fieldName, getBuyer().getID());
                case "product" -> node.put(fieldName, getProduct().getId());
                case "quantity" -> node.put(fieldName, getQuantity());
            }
        }
        return node;
    }

}
