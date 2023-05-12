package it.unibz.gangOf3.model.utils;

import it.unibz.gangOf3.model.User;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.SQLException;

public class ProductUtil {

    public static void createProduct(User owner, String name, String tag, String description, double price, String category) throws SQLException {
        DatabaseUtil.getConnection()
            .prepareStatement("INSERT INTO products (name, tag, description, price, category) VALUES ('" + name + "', '" + tag + "', '" + description + "', " + price + ", '" + category + "');")
            .execute();
    }

}
