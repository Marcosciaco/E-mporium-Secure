package it.unibz.gangOf3.model.classes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.repositories.UserRepository;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
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

    public String getName() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT name FROM products WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new NotFoundException("Product not found");
        }
        return rs.getString("name");
    }

    public String getTag() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT tag FROM products WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new NotFoundException("Product not found");
        }
        return rs.getString("tag");
    }

    public String getDescription() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT description FROM products WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new NotFoundException("Product not found");
        }
        return rs.getString("description");
    }

    public double getPrice() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT price FROM products WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new NotFoundException("Product not found");
        }
        return rs.getDouble("price");
    }

    public String getCategory() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT category FROM products WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new NotFoundException("Product not found");
        }
        return rs.getString("category");
    }

    public Blob getImg() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT image FROM products WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new NotFoundException("Product not found");
        }
        return rs.getBlob("image");
    }

    public void setImg(InputStream imgStream) {
        try {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("UPDATE products SET image = ? WHERE id = ?;");
            stmt.setBlob(1, imgStream);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getOwner() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT owner FROM products WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new NotFoundException("Product not found");
        }
        int ownerId = rs.getInt("owner");
        return UserRepository.getUserById(ownerId);
    }

    public int getStock() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT stock FROM products WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new NotFoundException("Product not found");
        }
        return rs.getInt("stock");
    }

    public int getStars() throws SQLException {
        return DatabaseUtil.getConnection()
            .prepareStatement("SELECT stars FROM products WHERE id = " + id + ";")
            .executeQuery()
            .getInt("stars");
    }

    public void updateRating() throws NotFoundException, SQLException {
        //Get the average rating of the product with productId
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT AVG(stars) AS avg FROM reviews WHERE product = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next()) {
            throw new NotFoundException("Product not found");
        }
        double avg = resultSet.getDouble("avg");

        String avgStr = String.valueOf(avg);
        if (avg == 0)
            avgStr = "NULL";

        //Update the rating in the products table
        PreparedStatement stmt2 = DatabaseUtil.getConnection()
            .prepareStatement("UPDATE products SET stars = ? WHERE id = ?;");
        if (avg != 0)
            stmt2.setDouble(1, avg);
        else
            stmt2.setNull(1, java.sql.Types.DOUBLE);
        stmt2.setInt(2, id);
        stmt2.executeUpdate();
    }

    public void delete() throws SQLException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("DELETE FROM products WHERE id = ?;");
        stmt.setInt(1, id);
        stmt.executeUpdate();
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
                case "owner" -> node.put(fieldName, getOwner().getUsername());
                case "stock" -> node.put(fieldName, getStock());
                case "stars" -> node.put(fieldName, getStars());
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
