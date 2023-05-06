package it.unibz.gangOf3.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:database.db");
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return connection;
    }

    public static void init() {
        InputStream assetFileIS = DatabaseUtil.class.getResourceAsStream("/backend/database/init.sql");
        if (assetFileIS == null) {
            throw new RuntimeException("Cannot find init.sql for database setup");
        }
        try {
            String sql = new String(assetFileIS.readAllBytes());
            Connection connection = getConnection();
            connection.createStatement().execute(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
