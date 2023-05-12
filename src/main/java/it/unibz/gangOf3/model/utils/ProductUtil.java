package it.unibz.gangOf3.model.utils;

import it.unibz.gangOf3.model.Product;
import it.unibz.gangOf3.model.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class ProductUtil {

    /**
     * Create a new product
     * @param owner The user that owns the product
     * @param name The name of the product
     * @param tag The tag of the product
     * @param description The description of the product
     * @param price The price of the product
     * @param category The category of the product
     * @return The ID of the created product
     * @throws SQLException
     * @throws NotFoundException
     */
    public static int createProduct(User owner, String name, String tag, String description, double price, String category) throws SQLException, NotFoundException {
       DatabaseUtil.getConnection()
            .prepareStatement("INSERT INTO products (name, tag, description, price, category, owner) VALUES ('" + name + "', '" + tag + "', '" + description + "', " + price + ", '" + category + "', " + owner.getID() + ");")
            .execute();
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT seq from sqlite_sequence WHERE name='products';")
            .executeQuery();
        if (!resultSet.next()) {
            throw new RuntimeException("Could not create product");
        }
        return resultSet.getInt("seq");
    }

    public static Product getProductById(int id) throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM products WHERE id = " + id + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Product not found");
        return new Product(id);
    }

    /**
     * Filter the given list of products by the given query
     * @param query The query to filter by
     *              The query can be a part of the tag or name or description
     * @param source The list of products to filter
     *               If the list is empty, the products will be loaded from the database
     * @param maxProducts The maximum number of products to load from the database
     *                    If -1, all products will be loaded
     * @throws SQLException
     */
    public static void filterProductsByQuery(String query, LinkedList<Product> source, int maxProducts) throws SQLException {
        if (source.size() == 0) {
            ResultSet resultSet = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM products WHERE name LIKE '%" + query + "%' OR tag LIKE '%" + query + "%' OR description LIKE '%" + query + "%' " + (maxProducts != -1 ? "LIMIT " + maxProducts : "" ) +  ";")
                .executeQuery();
            while (resultSet.next()) {
                source.add(new Product(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Product> toRemove = new LinkedList<>();
            for (Product product : source) {
                if (!product.getName().contains(query) && !product.getTag().contains(query) && !product.getDescription().contains(query)) {
                    toRemove.add(product);
                }
            }
            source.removeAll(toRemove);
        }
    }


    /**
     * Filter the given list of products by the given category
     * @param category The category to filter by
     * @param source The list of products to filter, if empty, all products will be loaded
     * @param maxProducts The maximum number of products to load, -1 for no limit
     * @throws SQLException
     */
    public static void filterProductsByCategory(String category, LinkedList<Product> source, int maxProducts) throws SQLException {
        if (source.size() == 0) {
            ResultSet resultSet = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM products WHERE category = '" + category + "' " + (maxProducts != -1 ? "LIMIT " + maxProducts : "" ) +  ";")
                .executeQuery();
            while (resultSet.next()) {
                source.add(new Product(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Product> toRemove = new LinkedList<>();
            for (Product product : source) {
                if (!product.getCategory().equals(category)) {
                    toRemove.add(product);
                }
            }
            source.removeAll(toRemove);
        }
    }

    /**
     * Filter the given list of products by the given price range
     * @param min The minimum price
     * @param max The maximum price
     * @param source The list of products to filter, if empty, all products will be loaded
     * @param maxProducts The maximum number of products to load, -1 for no limit
     * @throws SQLException
     */
    public static void filterProductsByPrice(double min, double max, LinkedList<Product> source, int maxProducts) throws SQLException {
        if (source.size() == 0) {
            ResultSet resultSet = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM products WHERE price >= " + min + " AND price <= " + max + " " + (maxProducts != -1 ? "LIMIT " + maxProducts : "" ) +  ";")
                .executeQuery();
            while (resultSet.next()) {
                source.add(new Product(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Product> toRemove = new LinkedList<>();
            for (Product product : source) {
                if (product.getPrice() < min || product.getPrice() > max) {
                    toRemove.add(product);
                }
            }
            source.removeAll(toRemove);
        }
    }

    /**
     * Filter the given list of products by the given owner
     * @param owner The owner to filter by
     * @param source The list of products to filter, if empty, all products will be loaded
     * @param maxProducts The maximum number of products to load, -1 for no limit
     * @throws SQLException
     */
    public static void filterProductsByOwner(User owner, LinkedList<Product> source, int maxProducts) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            ResultSet resultSet = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM products WHERE owner = " + owner.getID() + " " + (maxProducts != -1 ? "LIMIT " + maxProducts : "") + ";")
                .executeQuery();
            while (resultSet.next()) {
                source.add(new Product(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Product> toRemove = new LinkedList<>();
            for (Product product : source) {
                if (product.getOwner().getID() != owner.getID()) {
                    toRemove.add(product);
                }
            }
            source.removeAll(toRemove);
        }
    }

    /**
     * Get a list of random products
     * @param source The list to add the products to
     * @param max The maximum number of products to add
     */
    public static void getRandomProducts(LinkedList<Product> source, int max) throws SQLException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM products ORDER BY RANDOM() LIMIT " + max + ";")
            .executeQuery();
        while (resultSet.next()) {
            source.add(new Product(resultSet.getInt("id")));
        }
    }
}
