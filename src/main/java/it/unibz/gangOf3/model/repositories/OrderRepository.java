package it.unibz.gangOf3.model.repositories;

import it.unibz.gangOf3.model.classes.Order;
import it.unibz.gangOf3.model.classes.Product;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseInsertionUtil;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class OrderRepository {

    public static int createOrder(User buyer, Product product, int quantity) throws SQLException, NotFoundException {
        DatabaseInsertionUtil.insertData("orders", new String[]{"buyer", "product", "quantity"}, new String[]{buyer.getID() + "", product.getId() + "", quantity + ""});
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


    public static void filterOrdersByBuyer(User buyer, LinkedList<Order> source) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM orders WHERE buyer = ?;");
            stmt.setInt(1, buyer.getID());
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

    public static void filterOrdersBySeller(User seller, LinkedList<Order> source) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT orders.id " +
                    "FROM orders JOIN products p ON p.id = orders.product " +
                    "WHERE owner = ?;");
            stmt.setInt(1, seller.getID());
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

    public static void filterOrdersByProduct(Product product, LinkedList<Order> source) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM orders WHERE product = ?;");
            stmt.setInt(1, product.getId());
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
