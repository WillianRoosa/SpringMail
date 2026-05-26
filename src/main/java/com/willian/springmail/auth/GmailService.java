package com.willian.springmail.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.gmail.Gmail;

import com.google.api.services.gmail.model.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Properties;

@Component
public class GmailService {
    private Gmail getGmailClient() throws Exception {
        Credential credential = GmailAuth.authorize();
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Gmail.Builder(httpTransport, GmailAuth.JSON_FACTORY, credential)
                .setApplicationName("SpringMailApp")
                .build();
    }

    public void sendMail(String from, String to, String subject, String bodyHtml) {
        try {
            Gmail gmail = getGmailClient();

            Properties properties = new Properties();
            Session session = Session.getDefaultInstance(properties, null);

            MimeMessage mail = new MimeMessage(session);
            mail.setFrom(new InternetAddress(from));
            mail.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
            mail.setSubject(subject, "UTF-8");
            mail.setContent(bodyHtml, "text/html; charset=utf-8");

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mail.writeTo(buffer);

            String encodedMail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());
            Message message = new Message();
            message.setRaw(encodedMail);

            gmail.users().messages().send("me", message).execute();
            System.out.println("[GMAIL] Email enviado com sucesso para: " + to);
        } catch (Exception e) {
            System.err.println("[GMAIL] Erro ao enviar email para: " + to + " | " + e.getMessage());
            throw new RuntimeException("Falha ao enviar email para: " + to, e);
        }
    }
}
