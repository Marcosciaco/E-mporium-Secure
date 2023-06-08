package it.unibz.gangOf3.model.repositories;

import it.unibz.gangOf3.model.classes.Message;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseUtil;
import it.unibz.gangOf3.util.security.DESLab.DESWrapper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;

public class MessageRepository {

    public static int createMessage(User from, User to, String message) throws SQLException, NotFoundException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        String key = ChatRepository.generateSymmetricKey(from, to);
        message = DESWrapper.encrypt(message, key);

        PreparedStatement preparedStatement = DatabaseUtil.getConnection()
            .prepareStatement("INSERT INTO chat (user1, user2, message) VALUES (?, ?, ?);");
        preparedStatement.setInt(1, from.getID());
        preparedStatement.setInt(2, to.getID());
        preparedStatement.setString(3, message);
        preparedStatement.executeUpdate();

        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT seq from sqlite_sequence WHERE name='chat';")
            .executeQuery();
        if (!resultSet.next())
            throw new RuntimeException("Could not create message");
        return resultSet.getInt("seq");
    }

    public static Message getMessageById(int messageId) throws SQLException, NotFoundException {
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM chat WHERE id = ?;");
        stmt.setInt(1, messageId);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next()) {
            throw new NotFoundException("Message not found");
        }
        return new Message(messageId);
    }

    public static void filterBySince(User requestor, Timestamp since, LinkedList<Message> source, int max) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM chat WHERE time >= ? AND (user1 = ? OR user2 = ?) LIMIT ?;");
            stmt.setTimestamp(1, since);
            stmt.setInt(2, requestor.getID());
            stmt.setInt(3, requestor.getID());
            stmt.setInt(4, max);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()){
                source.add(new Message(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Message> toRemove = new LinkedList<>();
            for (Message message : source) {
                if (message.getTimestamp().before(since)){
                    toRemove.add(message);
                }
            }
            source.removeAll(toRemove);
        }
    }

    public static void filterByLatest(LinkedList<Message> source, int max) throws SQLException {
        if (source.size() != 0) {
            //sort by timestamp
            source.sort((o1, o2) -> {
                try {
                    return o1.getTimestamp().compareTo(o2.getTimestamp());
                } catch (SQLException | NotFoundException e) {
                    e.printStackTrace();
                    return 0;
                }
            });
            //remove all but the last max
            while (source.size() > max) {
                source.removeFirst();
            }
        }
    }

    public static void filterByUsers(User user1, User user2, LinkedList<Message> source, int max) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            PreparedStatement stmt = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM chat WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)  LIMIT ?;");
            stmt.setInt(1, user1.getID());
            stmt.setInt(2, user2.getID());
            stmt.setInt(3, user2.getID());
            stmt.setInt(4, user1.getID());
            stmt.setInt(5, max);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()){
                source.add(new Message(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Message> toRemove = new LinkedList<>();
            for (Message message : source) {
                if (!(message.getFrom().equals(user1) && message.getTo().equals(user2)) && !(message.getFrom().equals(user2) && message.getTo().equals(user1))){
                    toRemove.add(message);
                }
            }
            source.removeAll(toRemove);
        }
    }

    public static LinkedList<Integer> getChatPartners(User user) throws SQLException, NotFoundException {
        LinkedList<Integer> users = new LinkedList<>();
        PreparedStatement stmt = DatabaseUtil.getConnection()
            .prepareStatement("SELECT DISTINCT user1, user2 FROM chat WHERE user1 = ? OR user2 = ?;");
        stmt.setInt(1, user.getID());
        stmt.setInt(2, user.getID());
        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()){
            int user1 = resultSet.getInt("user1");
            int user2 = resultSet.getInt("user2");
            if (user1 != user.getID() && !users.contains(user1)){
                users.add(user1);
            } else if (user2 != user.getID() && !users.contains(user2)){
                users.add(user2);
            }
        }
        return users;
    }
}
