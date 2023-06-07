package it.unibz.gangOf3.model.repositories;

import it.unibz.gangOf3.email.EmailSender;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.exceptions.UserAlreadyExistsException;
import it.unibz.gangOf3.util.DatabaseUtil;
import it.unibz.gangOf3.util.security.RSALab.RSA;
import it.unibz.gangOf3.util.security.RSALab.RSAKeys;
import it.unibz.gangOf3.util.security.hashing.PasswordHasher;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;

public class UserRepository {

    public static void createUser(String username, String email, String password, String type, String emergencyEmail, String emergencyPhone) throws UserAlreadyExistsException, SQLException, IOException, MessagingException {
        if (username.length() < 3 || !(type.equals("seller") || type.equals("buyer")))
            throw new IllegalArgumentException("Invalid username or type");

        //Check password
        Pattern passwordPattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$");
        if (!passwordPattern.matcher(password).matches())
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain at least one digit, one lowercase and one uppercase letter");

        Pattern emailPattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        if (!emailPattern.matcher(email).matches())
            throw new IllegalArgumentException("Invalid email");
        if (!"".equals(emergencyEmail) && !emailPattern.matcher(emergencyEmail).matches())
            throw new IllegalArgumentException("Invalid emergency email");

        Pattern phonePattern = Pattern.compile("^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$");
        if (!"".equals(emergencyPhone) && !phonePattern.matcher(emergencyPhone).matches())
            throw new IllegalArgumentException("Invalid emergency phone");

        //Generate salt
        String salt = PasswordHasher.generateSalt(password.length() * 2L + password.charAt(0));

        //Hash password
        try {
            password = PasswordHasher.hashWithSalt(password, salt);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Error while hashing password");
        }

        String registrationTokenUUID = UUID.randomUUID().toString();
        PreparedStatement insertStmt = DatabaseUtil.getConnection()
            .prepareStatement("INSERT INTO users (username, email, password, salt, type, emergencyEmail, emergencyPhone, registrationToken) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
        insertStmt.setString(1, username);
        insertStmt.setString(2, email);
        insertStmt.setString(3, password);
        insertStmt.setString(4, salt);
        insertStmt.setString(5, type);
        insertStmt.setString(6, emergencyEmail);
        insertStmt.setString(7, emergencyPhone);
        insertStmt.setString(8, registrationTokenUUID);
        try {
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) {
                throw new UserAlreadyExistsException("User already exists");
            }
            throw e;
        }

        //Generate RSA key pair for user
        if (!"seller".equals(type)) {
            //Get id of user
            PreparedStatement getUserIdStmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM users WHERE email = ?;");
            getUserIdStmt.setString(1, email);
            ResultSet resultSet = getUserIdStmt.executeQuery();
            if (resultSet.next()) {
                int userId = resultSet.getInt("id");
                RSA rsa = new RSA();
                RSAKeys rsaKeys = rsa.generateKeys();
                PreparedStatement insertRsaKeysStmt = DatabaseUtil.getConnection()
                    .prepareStatement("INSERT INTO rsaKeys (user, e, d, n) VALUES (?, ?, ?, ?);");
                insertRsaKeysStmt.setInt(1, userId);
                insertRsaKeysStmt.setInt(2, rsaKeys.getE());
                insertRsaKeysStmt.setInt(3, rsaKeys.getD());
                insertRsaKeysStmt.setInt(4, rsaKeys.getN());
                insertRsaKeysStmt.executeUpdate();
            }
        }

        //Send email
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
