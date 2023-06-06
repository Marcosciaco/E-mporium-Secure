package it.unibz.gangOf3.model.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.email.EmailSender;
import it.unibz.gangOf3.model.exceptions.InvalidPasswordException;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.exceptions.UnconfirmedRegistrationException;
import it.unibz.gangOf3.util.DatabaseUtil;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class User {

    private static int loginCounter = 0;


    private String email;
    private int id;

    public User(String email) {
        this.email = email;
    }

    public User(int id) {
        this.id = id;
    }

    public String getEmail() throws SQLException {
        if (email == null) {
            email = DatabaseUtil.getConnection()
                .prepareStatement("SELECT email FROM users WHERE id = " + id + ";")
                .executeQuery()
                .getString("email");
        }
        return email;
    }

    public String getUsername() throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT username FROM users WHERE id = '" + getID() + "';")
            .executeQuery();
        if (!resultSet.next()) {
            throw new NotFoundException("User not found");
        }
        return resultSet.getString("username");
    }

    public int getID() throws SQLException, NotFoundException {
        if (id == 0) {
            ResultSet resultSet = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM users WHERE email = '" + email + "';")
                .executeQuery();
            if (!resultSet.next()) {
                throw new NotFoundException("User not found");
            }
            id = resultSet.getInt("id");
        }
        return id;
    }

    public ObjectNode login(String password) throws InvalidPasswordException, SQLException, UnconfirmedRegistrationException, NotFoundException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
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
        String sessionUUID = "" + (++loginCounter); //UUID.randomUUID().toString();
        DatabaseUtil.getConnection()
            .prepareStatement("UPDATE users SET sessionToken = '" + sessionUUID + "' WHERE email = '" + email + "';")
            .execute();
        result.put("token", sessionUUID);
        result.put("email", getEmail());
        result.put("username", getUsername());
        result.put("isSeller", isSeller());
        return result;
    }

    private boolean isSeller() throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT type FROM users WHERE id = '" + getID() + "';")
            .executeQuery();
        if (!resultSet.next()) {
            throw new NotFoundException("User not found");
        }
        return resultSet.getBoolean("type");
    }

    public void forgotPassword() throws SQLException, IOException, MessagingException, NotFoundException {
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }
        User toCompare = (User) obj;
        try {
            return toCompare.getID() == getID();
        } catch (SQLException | NotFoundException e) {
            return false;
        }
    }
}
