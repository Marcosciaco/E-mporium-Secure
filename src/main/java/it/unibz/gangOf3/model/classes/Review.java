package it.unibz.gangOf3.model.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.repositories.ProductRepository;
import it.unibz.gangOf3.model.repositories.UserRepository;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class  Review {

    private int id;

    public Review(int id) {
        this.id = id;
    }

    public User getWriter() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT user FROM reviews WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next())
            throw new NotFoundException("Writer of review not found");
        return UserRepository.getUserById(rs.getInt("user"));
    }

    public Product getProduct() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT product FROM reviews WHERE id = " + id + ";");
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Product of review not found");
        return ProductRepository.getProductById(resultSet.getInt("product"));
    }

    public void delete() throws SQLException, NotFoundException {
        Product product = getProduct();

        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("DELETE FROM reviews WHERE id = ?;");
        stmt.setInt(1, id);
        stmt.executeUpdate();

        product.updateRating();
    }

    public ObjectNode toJSON(ObjectMapper mapper) throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT * FROM reviews WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Review not found");
        ObjectNode review = mapper.createObjectNode();
        review.put("id", id);
        review.put("writer", new User(resultSet.getInt("user")).getUsername());
        review.put("product", resultSet.getInt("product"));
        review.put("stars", resultSet.getInt("stars"));
        review.put("comment", resultSet.getString("comment"));
        return review;
    }

}
