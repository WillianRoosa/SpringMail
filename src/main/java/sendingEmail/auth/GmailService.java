package sendingEmail.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

public class GmailService {
    private final Gmail gmailService;
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public GmailService(Credential credential) throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        gmailService = new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("SpringMailApp")
                .build();
    }

    public void sendEmail(String fromEmail, String to, String subject, String bodyText) throws MessagingException, IOException {
        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);
        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(fromEmail));
        email.addRecipient(RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);

        String encodeEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());

        Message message = new Message();
        message.setRaw(encodeEmail);

        try {
            gmailService.users().messages().send("me", message).execute();
            System.out.println("✅ Email enviado com sucesso para: " + to);
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            System.err.println("❌ Erro da API do Google (Código: " + e.getStatusCode() + "): " + e.getMessage());
            throw e;
        }
    }
}
