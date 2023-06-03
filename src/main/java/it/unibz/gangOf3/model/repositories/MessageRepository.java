package it.unibz.gangOf3.model.repositories;

import it.unibz.gangOf3.model.classes.Message;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.util.DatabaseInsertionUtil;
import it.unibz.gangOf3.util.DatabaseUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;

public class MessageRepository {

    public static int createMessage(User from, User to, String message) throws SQLException, NotFoundException {
        DatabaseInsertionUtil.insertData("chat", new String[]{"user1", "user2", "message"}, new String[]{from.getID() + "", to.getID() + "", message});
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT seq from sqlite_sequence WHERE name='chat';")
            .executeQuery();
        if (!resultSet.next())
            throw new RuntimeException("Could not create message");
        return resultSet.getInt("seq");
    }

    public static Message getMessageById(int messageId) throws SQLException, NotFoundException {
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT id FROM chat WHERE id = " + messageId + ";")
            .executeQuery();
        if (!resultSet.next()) {
            throw new NotFoundException("Message not found");
        }
        return new Message(messageId);
    }

    public static void filterBySince(Timestamp since, LinkedList<Message> source, int max) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            ResultSet resultSet = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM chat WHERE time >= '" + since.toString() + "' LIMIT " + max + ";")
                .executeQuery();
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
            ResultSet resultSet = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM chat WHERE (user1 = '" + user1.getID() + "' AND user2 = '" + user2.getID() + "') OR (user1 = '" + user2.getID() + "' AND user2 = '" + user1.getID() + "')  LIMIT " + max + ";")
                .executeQuery();
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
        ResultSet resultSet = DatabaseUtil.getConnection()
            .prepareStatement("SELECT DISTINCT user1, user2 FROM chat WHERE user1 = '" + user.getID() + "' OR user2 = '" + user.getID() + "';")
            .executeQuery();
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
