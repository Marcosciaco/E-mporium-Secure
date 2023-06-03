package it.unibz.gangOf3.model.repositories;

import it.unibz.gangOf3.model.classes.Product;
import it.unibz.gangOf3.model.classes.Review;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseInsertionUtil;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class ReviewRepository {

    public static void createReview(User user, int productId, int rating, String comment) throws SQLException, NotFoundException {
        DatabaseInsertionUtil.insertData("reviews", new String[]{"user", "stars", "comment", "product"}, new String[]{user.getID() + "", rating + "", comment, productId + ""});
        Product product = ProductRepository.getProductById(productId);
        product.updateRating();
    }

    public static Review getReviewById(int id) throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM reviews WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Review not found");
        return new Review(id);
    }

    public static LinkedList<Review> getReviewsForProduct(int productId) throws SQLException, NotFoundException {
        LinkedList<Review> reviews = new LinkedList<>();
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM reviews WHERE product = ?;");
        stmt.setInt(1, productId);
        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            reviews.add(getReviewById(resultSet.getInt("id")));
        }
        return reviews;
    }

}
