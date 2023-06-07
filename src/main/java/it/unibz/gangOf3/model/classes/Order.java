package it.unibz.gangOf3.model.classes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;
import it.unibz.gangOf3.util.security.RSALab.RSA;

import java.sql.PreparedStatement;
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
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT buyer FROM orders WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Order not found");
        return new User(resultSet.getInt("buyer"));
    }

    public Product getProduct() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT product FROM orders WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Order not found");
        return new Product(resultSet.getInt("product"));
    }

    public int getQuantity() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT quantity FROM orders WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Order not found");
        return resultSet.getInt("quantity");
    }

    public boolean isVerified() throws SQLException, NotFoundException {
        //Get signature from database
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT product, buyer, signature FROM orders WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new SQLException("Order not found");
        String signature = resultSet.getString("signature");

        //Get public key for the buyer
        User buyer = getBuyer();
        stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT d, n FROM rsaKeys WHERE id = ?;");
        stmt.setInt(1, buyer.getID());
        resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new SQLException("Buyer not found");
        int d = resultSet.getInt("d");
        int n = resultSet.getInt("n");

        //Decrypt signature
        String decryptedSignature;
        try {
            decryptedSignature = RSA.decrypt(signature, d, n);
        } catch (Exception e) {
            return false;
        }

        //Check if signature is valid
        String  expectedSignature = buyer.getUsername() + "#" + getProduct().getId() + "#" + getQuantity();

        return expectedSignature.equals(decryptedSignature);
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
                case "verified" -> node.put(fieldName, isVerified());
            }
        }
        return node;
    }

}
