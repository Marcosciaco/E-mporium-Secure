package it.unibz.gangOf3.model.repositories;

import it.unibz.gangOf3.email.EmailSender;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.UserAlreadyExistsException;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseInsertionUtil;
import it.unibz.gangOf3.util.DatabaseUtil;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserRepository {

    public static void createUser(String name, String email, String password, String type, String emergencyEmail, String emergencyPhone) throws UserAlreadyExistsException, SQLException, IOException, MessagingException {
        type = String.valueOf("seller".equals(type));
        String registrationTokenUUID = UUID.randomUUID().toString();
        DatabaseInsertionUtil.insertData("users", new String[]{"username", "email", "password", "type", "emergencyEmail", "emergencyPhone", "registrationToken"}, new String[]{name, email, password, type, emergencyEmail, emergencyPhone, registrationTokenUUID});
        InputStream registrationEmailStream = User.class.getClassLoader().getResourceAsStream("backend/email/registration.html");
        String registrationEmail = new String(registrationEmailStream.readAllBytes());
        EmailSender.sendEmail(email, "Confirm your email", registrationEmail
            .replace("{TOKEN}", registrationTokenUUID)
            .replace("{NAME}", name)
        );
    }

    public static User getUserByEmail(String email) throws SQLException, NotFoundException {
        email = "'" + email + "'";
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT email FROM users WHERE email = " + email + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("User not found");
        return new User(resultSet.getString("email"));
    }

    public static User getUserBySessionId(String sessionId) throws SQLException, NotFoundException {
        sessionId = "'" + sessionId + "'";
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT email FROM users WHERE sessionToken = " + sessionId + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("User not found");
        return new User(resultSet.getString("email"));
    }

    public static User getUserById(int userId) throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT email FROM users WHERE id = " + userId + ";")
            .executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("User not found");
        return new User(userId);
    }

}
