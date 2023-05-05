package it.unibz.gangOf3.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailSender {

    private static Properties prop;

    public static void init() throws IOException {
        InputStream configStream = EmailSender.class.getClassLoader().getResourceAsStream("backend/email/config.json");
        String config = new String(configStream.readAllBytes());

        //Parse config
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode configJson = objectMapper.readTree(config);

        //Set properties
        prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", String.valueOf(configJson.get("starttls").asBoolean()).toLowerCase());
        prop.put("mail.smtp.host", configJson.get("smtp_host").asText());
        prop.put("mail.smtp.port", String.valueOf(configJson.get("smtp_port").asInt()));
        prop.put("mail.smtp.ssl.trust", configJson.get("smtp_host").asText());
        prop.put("mail.smtp.sender.email", configJson.get("sender_email").asText());
        prop.put("mail.smtp.sender.username", configJson.get("username").asText());
        prop.put("mail.smtp.sender.password", configJson.get("password").asText());
    }

    private static Session getSession() {
        return Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(prop.getProperty("mail.smtp.sender.username"), prop.getProperty("mail.smtp.sender.password"));
            }
        });
    }

    public static void sendEmail(String targetEmail, String subject, String content) throws MessagingException {
        Message message = new MimeMessage(getSession());
        message.setFrom(new InternetAddress(prop.getProperty("mail.smtp.sender.email")));
        message.setRecipients(
            Message.RecipientType.TO, InternetAddress.parse(targetEmail));
        message.setSubject(subject);

        String msg = content;

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }

}
