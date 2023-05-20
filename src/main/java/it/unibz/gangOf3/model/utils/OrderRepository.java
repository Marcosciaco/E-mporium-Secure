package it.unibz.gangOf3.model.utils;

import it.unibz.gangOf3.model.Order;
import it.unibz.gangOf3.model.Product;
import it.unibz.gangOf3.model.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class OrderRepository {

    public static int createOrder(User buyer, Product product, int quantity) throws SQLException, NotFoundException {
        DatabaseUtil.getConnection()
            .prepareStatement("INSERT INTO orders (buyer, product, quantity) VALUES (" + buyer.getID() + ", " + product.getId() + ", " + quantity + ");")
            .executeUpdate();
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT seq from sqlite_sequence WHERE name='orders';")
            .executeQuery();
        if (!resultSet.next()) {
            throw new RuntimeException("Could not create order");
        }
        return resultSet.getInt("seq");
    }

    public static Order getOrderById(int id) throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM orders WHERE id = " + id + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Order not found");
        return new Order(id);
    }


    public static void filterOrdersByBuyer(User buyer, LinkedList<Order> source) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            ResultSet resultSet = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM orders WHERE buyer = " + buyer.getID() + ";")
                .executeQuery();
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

    public static void filterOrdersByProduct(Product product, LinkedList<Order> source) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            ResultSet resultSet = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM orders WHERE product = " + product.getId() + ";")
                .executeQuery();
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
