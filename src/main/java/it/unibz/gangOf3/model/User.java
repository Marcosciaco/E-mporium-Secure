package it.unibz.gangOf3.model;

import it.unibz.gangOf3.email.EmailSender;
import it.unibz.gangOf3.model.exceptions.InvalidPasswordException;
import it.unibz.gangOf3.model.exceptions.UnconfirmedRegistrationException;
import it.unibz.gangOf3.model.exceptions.UserAlreadyExistsException;
import it.unibz.gangOf3.model.exceptions.UserNotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class User {

    private String email;

    public User(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() throws SQLException, UserNotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT username FROM users WHERE email = '" + email + "';")
            .executeQuery();
        if (!resultSet.next()) {
            throw new UserNotFoundException("User not found");
        }
        return resultSet.getString("username");
    }

    public String login(String password) throws InvalidPasswordException, SQLException, UnconfirmedRegistrationException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT password, registrationToken FROM users WHERE email = '" + email + "';")
            .executeQuery();
        if (!resultSet.next()) {
            throw new InvalidPasswordException("Invalid email or password");
        }
        String dbPassword = resultSet.getString("password");
        String registrationToken = resultSet.getString("registrationToken");
        //FIXME switch the order of the following if statements
        if (registrationToken != null) {
            throw new UnconfirmedRegistrationException("Please confirm your email");
        }
        if (!dbPassword.equals(password)) {
            throw new InvalidPasswordException("Invalid email or password");
        }
        String sessionUUID = UUID.randomUUID().toString();
        DatabaseUtil.getConnection()
            .prepareStatement("UPDATE users SET sessionToken = '" + sessionUUID + "' WHERE email = '" + email + "';")
            .execute();
        return sessionUUID;
    }

    public void forgotPassword() throws SQLException, IOException, MessagingException, UserNotFoundException {
        String resetUUID = UUID.randomUUID().toString();
        DatabaseUtil.getConnection()
            .prepareStatement("UPDATE users SET forgotToken = '" + resetUUID + "' WHERE email = '" + email + "';")
            .execute();
        InputStream emailTemplate = getClass().getClassLoader().getResourceAsStream("backend/email/reset_password.html");
        String emailBody = new String(emailTemplate.readAllBytes());
        emailBody = emailBody
            .replace("{TOKEN}", resetUUID)
            .replace("{NAME}", getUsername());
        EmailSender.sendEmail(email, "Reset password", emailBody);
    }

}
