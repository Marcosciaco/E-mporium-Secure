package it.unibz.gangOf3.model.repositories;

import it.unibz.gangOf3.model.classes.Product;
import it.unibz.gangOf3.model.classes.Review;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class ReviewRepository {

    public static void createReview(User user, int productId, int rating, String comment) throws SQLException, NotFoundException {
        if (comment.length() < 3 || rating < 1 || rating > 5)
            throw new IllegalArgumentException("Invalid review data");
        Product product = ProductRepository.getProductById(productId);
        PreparedStatement insertStmt = DatabaseUtil.getConnection()
            .prepareStatement("INSERT INTO reviews (user, stars, comment, product) VALUES (?, ?, ?, ?);");
        insertStmt.setInt(1, user.getID());
        insertStmt.setInt(2, rating);
        insertStmt.setString(3, comment);
        insertStmt.setInt(4, productId);
        insertStmt.executeUpdate();
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
