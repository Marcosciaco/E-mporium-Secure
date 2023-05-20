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

    public void updateRating() throws NotFoundException, SQLException {
        //Get the average rating of the product with productId
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT AVG(stars) AS avg FROM reviews WHERE product = " + id + ";")
            .executeQuery();
        if (!resultSet.next()) {
            throw new NotFoundException("Product not found");
        }
        double avg = resultSet.getDouble("avg");

        String avgStr = String.valueOf(avg);
        if (avg == 0)
            avgStr = "NULL";

        //Update the rating in the products table
        DatabaseUtil.getConnection()
            .prepareStatement("UPDATE products SET stars = " + avgStr + " WHERE id = " + id + ";")
            .executeUpdate();
    }

    public void delete() throws SQLException {
        DatabaseUtil.getConnection()
            .prepareStatement("DELETE FROM products WHERE id = " + id + ";")
            .executeUpdate();
    }

    public ObjectNode getAsJSON(ArrayNode fields, ObjectMapper mapper) throws SQLException, NotFoundException {
        ObjectNode node = mapper.createObjectNode();
        for (Iterator<JsonNode> it = fields.elements(); it.hasNext(); ) {
            JsonNode element = it.next();
            String fieldName = element.asText();
            switch (fieldName) {
                case "id" -> node.put(fieldName, id);
                case "name" -> node.put(fieldName, getName());
                case "tag" -> node.put(fieldName, getTag());
                case "description" -> node.put(fieldName, getDescription());
                case "price" -> node.put(fieldName, getPrice());
                case "category" -> node.put(fieldName, getCategory());
                case "img" -> node.put(fieldName, getImg());
                case "owner" -> node.put(fieldName, getOwner().getID());
            }
        }
        return node;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Product)) {
            return false;
        }
        Product product = (Product) obj;
        return product.getId() == getId();
    }
}
