package it.unibz.gangOf3.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseInsertionUtil {

    /**
     * Insert data into the database.
     */
    public static void insertData(String table, String[] columns, String[] values) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO " + table + " (");
        for (String column : columns) {
            sql.append(column).append(", ");
        }
        sql.delete(sql.length() - 2, sql.length());
        sql.append(") VALUES (");
        for (String value : values) {
            sql.append("?").append(", ");
        }
        sql.delete(sql.length() - 2, sql.length());
        sql.append(");");

        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement(sql.toString());
        for (int i = 0; i < values.length; i++) {
            stmt.setString(i + 1, values[i]);
        }
        stmt.executeUpdate();
    }

}
