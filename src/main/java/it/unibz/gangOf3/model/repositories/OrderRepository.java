package it.unibz.gangOf3.model.repositories;

import it.unibz.gangOf3.model.classes.Order;
import it.unibz.gangOf3.model.classes.Product;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.InvalidQuantityException;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class OrderRepository {

    public static int createOrder(User buyer, Product product, int quantity, String signature) throws SQLException, NotFoundException, InvalidQuantityException {
        if (quantity >= product.getStock())
            throw new InvalidQuantityException("Not enough products in stock");
        if (quantity < 1)
            throw new InvalidQuantityException("Quantity must be greater than 0");
        //Create order in database
        PreparedStatement insertStmt = DatabaseUtil.getConnection()
            .prepareStatement("INSERT INTO orders (buyer, product, quantity, signature) VALUES (?, ?, ?, ?);");
        insertStmt.setInt(1, buyer.getID());
        insertStmt.setInt(2, product.getId());
        insertStmt.setInt(3, quantity);
        insertStmt.setString(4, signature);
        insertStmt.executeUpdate();
        //Update stock
        PreparedStatement updateStmt = DatabaseUtil.getConnection()
            .prepareStatement("UPDATE products SET stock = stock - ? WHERE id = ?;");
        updateStmt.setInt(1, quantity);
        updateStmt.setInt(2, product.getId());
        updateStmt.executeUpdate();
        //Get order id
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT seq from sqlite_sequence WHERE name='orders';")
            .executeQuery();
        if (!resultSet.next()) {
            throw new RuntimeException("Could not create order");
        }
        return resultSet.getInt("seq");
    }

    public static Order getOrderById(int id) throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM orders WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Order not found");
        return new Order(id);
    }


    public static void filterOrdersByBuyer(User buyer, User requestor, LinkedList<Order> source) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT orders.id " +
                    "FROM orders JOIN products p ON p.id = orders.product " +
                    "WHERE (p.owner = ? AND orders.buyer = ?) OR (orders.buyer = ? AND orders.buyer = ?);");
            stmt.setInt(1, requestor.getID());
            stmt.setInt(2, buyer.getID());
            stmt.setInt(3, requestor.getID());
            stmt.setInt(4, buyer.getID());
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                source.add(new Order(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Order> toRemove = new LinkedList<>();
            for (Order order : source) {
                if (!order.getBuyer().equals(buyer)){
                    toRemove.add(order);
                }
            }
            source.removeAll(toRemove);
        }
    }

    public static void filterOrdersBySeller(User seller, User requestor, LinkedList<Order> source) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT orders.id " +
                    "FROM orders JOIN products p ON p.id = orders.product " +
                    "WHERE (p.owner = ? AND orders.buyer = ?) OR (p.owner = ? AND p.owner = ?);");
            stmt.setInt(1, seller.getID());
            stmt.setInt(2, requestor.getID());
            stmt.setInt(3, seller.getID());
            stmt.setInt(4, requestor.getID());
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                source.add(new Order(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Order> toRemove = new LinkedList<>();
            for (Order order : source) {
                if (!order.getProduct().getOwner().equals(seller)){
                    toRemove.add(order);
                }
            }
            source.removeAll(toRemove);
        }
    }

    public static void filterOrdersByProduct(Product product, User requestor, LinkedList<Order> source) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id " +
                    "FROM orders JOIN products p on p.id = orders.product " +
                    "WHERE orders.product = ? AND (orders.buyer = ? OR p.owner = ?);");
            stmt.setInt(1, product.getId());
            stmt.setInt(2, requestor.getID());
            stmt.setInt(3, requestor.getID());
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()){
                source.add(new Order(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Order> toRemove = new LinkedList<>();
            for (Order order : source) {
                if (!order.getProduct().equals(product)) {
                    toRemove.add(order);
                }
            }
            source.removeAll(toRemove);
        }
    }



}
