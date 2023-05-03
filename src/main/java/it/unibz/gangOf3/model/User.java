package it.unibz.gangOf3.model;

import it.unibz.gangOf3.model.exceptions.InvalidPasswordException;
import it.unibz.gangOf3.model.exceptions.UserAlreadyExistsException;
import it.unibz.gangOf3.model.exceptions.UserNotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {

    private String email;

    public User(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String login(String password) throws InvalidPasswordException, SQLException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT password FROM users WHERE email = '" + email + "';")
            .executeQuery();
        if (!resultSet.next()) {
            throw new InvalidPasswordException("Invalid email or password");
        }
        String dbPassword = resultSet.getString("password");
        if (!dbPassword.equals(password)) {
            throw new InvalidPasswordException("Invalid email or password");
        }
        return "placeholder //TODO: generate token"; //TODO: generate token
    }









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

    public static User getUser(String email) throws SQLException, UserNotFoundException {
        email = "'" + email + "'";
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT email FROM users WHERE email = " + email + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new UserNotFoundException("User not found");
        return new User(
            resultSet.getString("email"));
    }

}
