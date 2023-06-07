package it.unibz.gangOf3.model.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.email.EmailSender;
import it.unibz.gangOf3.model.exceptions.InvalidPasswordException;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.exceptions.UnconfirmedRegistrationException;
import it.unibz.gangOf3.util.DatabaseUtil;
import it.unibz.gangOf3.util.security.hashing.PasswordHasher;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class User {

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
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT email FROM users WHERE id = ?;");
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            email = resultSet.getString("email");
        }
        return email;
    }

    public String getUsername() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT username FROM users WHERE id = ?;");
        stmt.setInt(1, getID());
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next()) {
            throw new NotFoundException("User not found");
        }
        return resultSet.getString("username");
    }

    public int getID() throws SQLException, NotFoundException {
        if (id == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM users WHERE email = ?;");
            stmt.setString(1, email);
            ResultSet resultSet = stmt.executeQuery();
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
        verifyPassword(password);

        //Get if buyer or seller
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT type FROM users WHERE id = ?;");
        stmt.setInt(1, getID());
        ResultSet resultSet = stmt.executeQuery();
        boolean type = resultSet.getBoolean("type");
        System.out.println(type);
        if (!type) { //user is buyer
            //Get user's rsa key with private exponent
            stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT e, n FROM rsaKeys WHERE user = ?;");
            stmt.setInt(1, getID());
            resultSet = stmt.executeQuery();
            int e = resultSet.getInt("e");
            int n = resultSet.getInt("n");
            ObjectNode rsaKey = mapper.createObjectNode();
            rsaKey.put("e", e);
            rsaKey.put("n", n);
            result.set("rsaKey", rsaKey);
        }

        String sessionUUID = UUID.randomUUID().toString();
        PreparedStatement stmt2 = DatabaseUtil.getConnection()
            .prepareStatement("UPDATE users SET sessionToken = ? WHERE email = ?;");
        stmt2.setString(1, sessionUUID);
        stmt2.setString(2, getEmail());
        stmt2.execute();
        result.put("token", sessionUUID);
        result.put("email", getEmail());
        result.put("username", getUsername());
        result.put("isSeller", isSeller());
        return result;
    }

    public void verifyPassword(String password) throws SQLException, InvalidPasswordException, UnconfirmedRegistrationException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT password, salt, registrationToken FROM users WHERE email = ?;");
        stmt.setString(1, getEmail());
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next()) {
            throw new InvalidPasswordException("Invalid email or password");
        }
        String dbPassword = resultSet.getString("password");
        String salt = resultSet.getString("salt");
        String registrationToken = resultSet.getString("registrationToken");
        if (!PasswordHasher.verify(password, dbPassword, salt)) {
            throw new InvalidPasswordException("Invalid email or password");
        }
        if (registrationToken != null) {
            throw new UnconfirmedRegistrationException("Please confirm your email");
        }
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
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("UPDATE users SET forgotToken = ? WHERE email = ?;");
        stmt.setString(1, resetUUID);
        stmt.setString(2, getEmail());
        stmt.execute();
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
