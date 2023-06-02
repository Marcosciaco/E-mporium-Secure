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

    public static void filterBySender(User sender, LinkedList<Message> source, int max) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            ResultSet resultSet = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM chat WHERE user1 = '" + sender.getID() + "' LIMIT " + max + ";")
                .executeQuery();
            while (resultSet.next()){
                source.add(new Message(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Message> toRemove = new LinkedList<>();
            for (Message message : source) {
                if (!message.getFrom().equals(sender)){
                    toRemove.add(message);
                }
            }
            source.removeAll(toRemove);
        }
    }

    public static void filterByReceiver(User receiver, LinkedList<Message> source, int max) throws SQLException, NotFoundException {
        if (source.size() == 0) {
            ResultSet resultSet = DatabaseUtil.getConnection()
                .prepareStatement("SELECT id FROM chat WHERE user2 = '" + receiver.getID() + "' LIMIT " + max + ";")
                .executeQuery();
            while (resultSet.next()){
                source.add(new Message(resultSet.getInt("id")));
            }
        } else {
            LinkedList<Message> toRemove = new LinkedList<>();
            for (Message message : source) {
                if (!message.getTo().equals(receiver)){
                    toRemove.add(message);
                }
            }
            source.removeAll(toRemove);
        }
    }

}
