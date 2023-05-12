package it.unibz.gangOf3.model;

import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.SQLException;

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
            .prepareStatement("SELECT img FROM products WHERE id = " + id + ";")
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

    public void delete() throws SQLException {
        DatabaseUtil.getConnection()
            .prepareStatement("DELETE FROM products WHERE id = " + id + ";")
            .executeUpdate();
    }

}
