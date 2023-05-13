package it.unibz.gangOf3.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.SQLException;
import java.util.Iterator;

public class Product {

    private int id;

    public Product(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() throws SQLException {
        return DatabaseUtil.getConnection()
            .prepareStatement("SELECT name FROM products WHERE id = " + id + ";")
            .executeQuery()
            .getString("name");
    }

    public String getTag() throws SQLException {
        return DatabaseUtil.getConnection()
            .prepareStatement("SELECT tag FROM products WHERE id = " + id + ";")
            .executeQuery()
            .getString("tag");
    }

    public String getDescription() throws SQLException {
        return DatabaseUtil.getConnection()
            .prepareStatement("SELECT description FROM products WHERE id = " + id + ";")
            .executeQuery()
            .getString("description");
    }

    public double getPrice() throws SQLException {
        return DatabaseUtil.getConnection()
            .prepareStatement("SELECT price FROM products WHERE id = " + id + ";")
            .executeQuery()
            .getDouble("price");
    }

    public String getCategory() throws SQLException {
        return DatabaseUtil.getConnection()
            .prepareStatement("SELECT category FROM products WHERE id = " + id + ";")
            .executeQuery()
            .getString("category");
    }

    public String getImg() throws SQLException {
        return DatabaseUtil.getConnection()
            .prepareStatement("SELECT image FROM products WHERE id = " + id + ";")
            .executeQuery()
            .getString("img");
    }

    public User getOwner() throws SQLException {
        return new User(
            DatabaseUtil.getConnection()
                .prepareStatement("SELECT owner FROM products WHERE id = " + id + ";")
                .executeQuery()
                .getInt("owner")
        );
    }

    public void delete() throws SQLException {
        DatabaseUtil.getConnection()
            .prepareStatement("DELETE FROM products WHERE id = " + id + ";")
            .executeUpdate();
    }

    //TODO: refactor this maybe?
    public ObjectNode getAsJSON(ArrayNode fields, ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        for (Iterator<JsonNode> it = fields.elements(); it.hasNext(); ) {
            JsonNode element = it.next();
            String fieldName = element.asText();
            switch (fieldName) {
                case "id" -> node.put(fieldName, id);
                case "name" -> {
                    try {
                        node.put(fieldName, getName());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case "tag" -> {
                    try {
                        node.put(fieldName, getTag());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case "description" -> {
                    try {
                        node.put(fieldName, getDescription());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case "price" -> {
                    try {
                        node.put(fieldName, getPrice());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case "category" -> {
                    try {
                        node.put(fieldName, getCategory());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case "img" -> {
                    try {
                        node.put(fieldName, getImg());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case "owner" -> {
                    try {
                        node.put(fieldName, getOwner().getID());
                    } catch (SQLException | NotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return node;
    }

}
