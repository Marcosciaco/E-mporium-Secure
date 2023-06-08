package it.unibz.gangOf3.model.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.repositories.UserRepository;
import it.unibz.gangOf3.util.DatabaseUtil;
import it.unibz.gangOf3.util.security.DESLab.DESWrapper;
import it.unibz.gangOf3.util.security.DESLab.DiffieHellman;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class Message {

    private int id;
    private String message;

    public Message(int id){
        this.id = id;
    }

    public String getMessage() throws SQLException, NotFoundException {
        if (message == null){
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT message from chat WHERE id = ?;");
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next())
                throw new NotFoundException("Message not found");
            message = resultSet.getString("message");
        }
        return message;
    }

    public User getFrom() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT user1 from chat WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Message not found");
        int userID = resultSet.getInt("user1");
        return UserRepository.getUserById(userID);
    }

    public User getTo() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT user2 from chat WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Message not found");
        int userID = resultSet.getInt("user2");
        return UserRepository.getUserById(userID);
    }

    public Timestamp getTimestamp() throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT time from chat WHERE id = ?;");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next())
            throw new NotFoundException("Message not found");
        return resultSet.getTimestamp("time");
    }

    public ObjectNode getAsJson(ObjectMapper mapper) throws SQLException, NotFoundException, ParseException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        User from = getFrom();
        User to = getTo();
        String key = DiffieHellman.getSharedKey(from, to);
        String message = getMessage();
        message = DESWrapper.decrypt(message, key);

        ObjectNode node = mapper.createObjectNode();
        node.put("from", from.getEmail());
        node.put("to", to.getEmail());
        node.put("message", message);
        node.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(getTimestamp().toInstant()));
        return node;
    }

}
