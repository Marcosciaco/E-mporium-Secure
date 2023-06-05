package it.unibz.gangOf3.model.repositories;

import it.unibz.gangOf3.email.EmailSender;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.exceptions.UserAlreadyExistsException;
import it.unibz.gangOf3.util.DatabaseUtil;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;

public class UserRepository {

    public static void createUser(String username, String email, String password, String type, String emergencyEmail, String emergencyPhone) throws UserAlreadyExistsException, SQLException, IOException, MessagingException {
        if (username.length() < 3 || !(type.equals("seller") || type.equals("buyer")))
            throw new IllegalArgumentException("Username must be at least 3 characters long");

        //Check password
        Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$");
        if (!pattern.matcher(password).matches())
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain at least one digit, one lowercase and one uppercase letter");

        //TODO: regex check for emails and phone

        type = String.valueOf("seller".equals(type));
        String registrationTokenUUID = UUID.randomUUID().toString();
        PreparedStatement insertStmt = DatabaseUtil.getConnection()
            .prepareStatement("INSERT INTO users (username, email, password, type, emergencyEmail, emergencyPhone, registrationToken) VALUES (?, ?, ?, ?, ?, ?, ?);");
        insertStmt.setString(1, username);
        insertStmt.setString(2, email);
        insertStmt.setString(3, password);
        insertStmt.setString(4, type);
        insertStmt.setString(5, emergencyEmail);
        insertStmt.setString(6, emergencyPhone);
        insertStmt.setString(7, registrationTokenUUID);
        try {
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new UserAlreadyExistsException("User already exists");
            }
            throw e;
        }
        InputStream registrationEmailStream = User.class.getClassLoader().getResourceAsStream("backend/email/registration.html");
        String registrationEmail = new String(registrationEmailStream.readAllBytes());
        EmailSender.sendEmail(email, "Confirm your email", registrationEmail
            .replace("{TOKEN}", registrationTokenUUID)
            .replace("{NAME}", username)
        );
    }

    public static User getUserByEmail(String email) throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT email FROM users WHERE email = ?;");
        stmt.setString(1, email);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("User not found");
        return new User(resultSet.getString("email"));
    }

    public static User getUserByUsername(String username) throws SQLException, NotFoundException {
        username = "'" + username + "'";
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM users WHERE username = " + username + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("User not found");
        return new User(resultSet.getInt("id"));
    }

    public static User getUserBySessionId(String sessionId) throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT email FROM users WHERE sessionToken = ?;");
        stmt.setString(1, sessionId);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("User not found");
        return new User(resultSet.getString("email"));
    }

    public static User getUserById(int userId) throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT email FROM users WHERE id = ?;");
        stmt.setInt(1, userId);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("User not found");
        return new User(userId);
    }

}
