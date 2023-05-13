package it.unibz.gangOf3.model.utils;

import it.unibz.gangOf3.model.Product;
import it.unibz.gangOf3.model.Review;
import it.unibz.gangOf3.model.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class ReviewRepository {

    public static void createReview(User user, int productId, int rating, String comment) throws SQLException, NotFoundException {
        DatabaseUtil.getConnection()
            .prepareStatement("INSERT INTO reviews (user, stars, comment, product) VALUES ('" + user.getID() + "', " + rating + ", '" + comment + "', " + productId + ");")
            .executeUpdate();

        Product product = ProductRepository.getProductById(productId);
        product.updateRating();
    }

    public static Review getReviewById(int id) throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM reviews WHERE id = " + id + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Review not found");
        return new Review(id);
    }

    public static LinkedList<Review> getReviewsForProduct(int productId) throws SQLException {
        LinkedList<Review> reviews = new LinkedList<>();
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM reviews WHERE product = " + productId + ";")
            .executeQuery();
        while (resultSet.next()) {
            reviews.add(new Review(resultSet.getInt("id")));
        }
        return reviews;
    }

}
