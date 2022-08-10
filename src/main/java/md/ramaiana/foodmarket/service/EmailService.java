package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.model.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String from;
    @Value("${spring.mail.password}")
    private String password;
    @Value("${frontendUrl")
    private String frontendUrl;
    private final Session session;

    public EmailService() {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(from, password);

            }

        });
    }

    public void sendPasswordResetLink(AppUser appUser, String token) {
        String text = "<html><body><div>Hello!<br>" +
                "Someone has requested to reset your password.<br>" +
                "To reset your password please follow this link, if it was not you," +
                "please ignore this message. <br><br>" +
                "<a href='" + frontendUrl + "createNewPassword/" + token + "'>Link</a>" + "<br><br>" +
                "Thank you!</div></body></html>";
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(appUser.getEmail()));
            message.setSubject("Reset your password");
            message.setText(text, "utf-8", "html");
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
