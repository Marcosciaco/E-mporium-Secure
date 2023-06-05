package it.unibz.gangOf3.model.repositories;

import it.unibz.gangOf3.model.classes.Product;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class ProductRepository {

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
    public static int createProduct(User owner, String name, String tag, String description, double price, String category, int stock, String image) throws SQLException, NotFoundException {
        if (name.length() < 3 || tag.length() < 3 || description.length() < 3 || price < 0 || category.length() < 3 || stock < 1 || image.length() < 3)
            throw new IllegalArgumentException("Invalid product data");

        if (!image.startsWith("data:image/")) {
            throw new IllegalArgumentException("Invalid image URL");
        }

        PreparedStatement insertStmt = DatabaseUtil.getConnection()
            .prepareStatement("INSERT INTO products (name, tag, description, price, category, owner, stock, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
        insertStmt.setString(1, name);
        insertStmt.setString(2, tag);
        insertStmt.setString(3, description);
        insertStmt.setDouble(4, price);
        insertStmt.setString(5, category);
        insertStmt.setInt(6, owner.getID());
        insertStmt.setInt(7, stock);
        insertStmt.setString(8, image);
        insertStmt.executeUpdate();
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT seq from sqlite_sequence WHERE name='products';")
            .executeQuery();
        if (!resultSet.next()) {
            throw new RuntimeException("Could not create product");
        }
        return resultSet.getInt("seq");
    }


    /**
     * find a product by its id
     * @param id
     * @return Product with that id
     * @throws SQLException
     * @throws NotFoundException
     */
    public static Product getProductById(int id) throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM products WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
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
    public static void filterProductsByQuery(String query, LinkedList<Product> source, int maxProducts) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM products WHERE name LIKE ? OR tag LIKE ? OR description LIKE ? LIMIT ?;");
            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");
            stmt.setString(3, "%" + query + "%");
            if (maxProducts != -1)
                stmt.setInt(4, maxProducts);
            else
                stmt.setInt(4, 1000000000); //Here you can see a quick fix for the problem of the limit
            ResultSet resultSet = stmt.executeQuery();
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
    public static void filterProductsByCategory(String category, LinkedList<Product> source, int maxProducts) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM products WHERE category = ? LIMIT ?;");
            stmt.setString(1, category);
            if (maxProducts != -1)
                stmt.setInt(2, maxProducts);
            else
                stmt.setInt(2, 1000000000); //Here you can see a quick fix for the problem of the limit
            ResultSet resultSet = stmt.executeQuery();
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
    public static void filterProductsByPrice(double min, double max, LinkedList<Product> source, int maxProducts) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM products WHERE price >= ? AND price <= ? LIMIT ?;");
            stmt.setDouble(1, min);
            stmt.setDouble(2, max);
            if (maxProducts != -1)
                stmt.setInt(3, maxProducts);
            else
                stmt.setInt(3, 1000000000); //Here you can see a quick fix for the problem of the limit
            ResultSet resultSet = stmt.executeQuery();
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
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM products WHERE owner = ? LIMIT ?;");
            stmt.setInt(1, owner.getID());
            if (maxProducts != -1)
                stmt.setInt(2, maxProducts);
            else
                stmt.setInt(2, 1000000000); //Here you can see a quick fix for the problem of the limit
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                source.add(new Product(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Product> toRemove = new LinkedList<>();
            for (Product product : source) {
                if (product.getOwner().equals(owner)) {
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
    public static void getRandomProducts(LinkedList<Product> source, int max) throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM products ORDER BY RANDOM() LIMIT ?;");
        stmt.setInt(1, max);
        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            source.add(getProductById(resultSet.getInt("id")));
        }
    }
}
