package it.unibz.gangOf3.model.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class  Review {

    private int id;

    public Review(int id) {
        this.id = id;
    }

    public User getWriter() throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT user FROM reviews WHERE id = " + id + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Writer of review not found");
        return new User(resultSet.getInt("user"));
    }

    public Product getProduct() throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT product FROM reviews WHERE id = " + id + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Product of review not found");
        return new Product(resultSet.getInt("product"));
    }

    public void delete() throws SQLException, NotFoundException {
        Product product = getProduct();

        DatabaseUtil.getConnection()
            .prepareStatement("DELETE FROM reviews WHERE id = " + id + ";")
            .executeUpdate();

        product.updateRating();
    }

    public ObjectNode toJSON(ObjectMapper mapper) throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT * FROM reviews WHERE id = " + id + ";")
            .executeQuery();
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
