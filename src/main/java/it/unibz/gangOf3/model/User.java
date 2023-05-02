package it.unibz.gangOf3.model;

import it.unibz.gangOf3.model.exceptions.UserAlreadyExistsException;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.SQLException;

public class User {

    public static void createUser(String name, String email, String password, String type, String emergencyEmail, String emergencyPhone) throws UserAlreadyExistsException, SQLException {
        name = "'" + name + "'";
        email = "'" + email + "'";
        password = "'" + password + "'";
        type = "'" + "seller".equals(type) + "'";
        emergencyEmail = emergencyEmail != null ? "'" + emergencyEmail + "'" : "NULL";
        emergencyPhone = emergencyPhone != null ? "'" + emergencyPhone + "'" : "NULL";
        //FIXME: SQL injection
        DatabaseUtil.getConnection()
            .prepareStatement("INSERT INTO users (name, email, password, type, emergencyEmail, emergencyPhone) VALUES (" + name + ", " + email + ", " + password + ", " + type.equals("seller") + ", " + emergencyEmail + ", " + emergencyPhone + ");")
            .execute();
    }

}
