package it.unibz.gangOf3.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

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
            importSQL(getConnection(), assetFileIS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Import SQL from an InputStream
     * See https://stackoverflow.com/a/1498029 for more info
     * @param conn Database Connection
     * @param in InputStream to read SQL from
     * @throws SQLException
     */
    private static void importSQL(Connection conn, InputStream in) throws SQLException {
        Scanner s = new Scanner(in);
        s.useDelimiter("(;(\r)?\n)|(--\n)");
        Statement st = null;
        try
        {
            st = conn.createStatement();
            while (s.hasNext())
            {
                String line = s.next();
                if (line.startsWith("/*!") && line.endsWith("*/"))
                {
                    int i = line.indexOf(' ');
                    line = line.substring(i + 1, line.length() - " */".length());
                }

                if (line.trim().length() > 0)
                {
                    st.execute(line);
                }
            }
        }
        finally
        {
            if (st != null) st.close();
        }
    }

}
