package sendingEmail.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sendingEmail.config.MailProperties;
import sendingEmail.dto.ContactRequest;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public EmailService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    private boolean enviar(String destinatarioEmail, String destinatarioNome, String assunto, String corpoHTML) {
        if (destinatarioEmail == null || destinatarioEmail.trim().isEmpty()) {
            System.err.println("❌ E-mail do destinatário está vazio.");
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailProperties.getFrom(), "Willian Rosa - Portfólio!");
            helper.setTo(destinatarioEmail);
            helper.setSubject(assunto);
            helper.setText(corpoHTML, true);

            mailSender.send(message);
            System.out.println("✅ E-mail enviado para " + destinatarioEmail);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean enviarNotificacaoContato(ContactRequest request) {
        String assunto = "📩 Novo contato do portfólio";
        String corpo = """
                <h2>Nova mensagem recebida</h2>
                <p><b>Nome:</b> %s</p>
                <p><b>Email:</b> %s</p>
                <p><b>WhatsApp:</b> %s</p>
                <p><b>Mensagem:</b> %s</p>
                """.formatted(request.getNome(), request.getEmail(), request.getTelefone(), request.getMensagem());
        return enviar(mailProperties.getAdminEmail(), "Willian Rosa", assunto, corpo);
    }

    public boolean enviarRespostaAutomatica(ContactRequest request) {
        String assunto = "✅ Recebemos sua mensagem!";
        String corpo = """
                <h2>Olá %s,</h2>
                <p>Obrigado por entrar em contato! Recebemos sua mensagem e responderemos em breve.</p>
                <p>Atenciosamente,<br><b>Willian Rosa - Developer</b></p>
                """.formatted(request.getNome());
        return enviar(request.getEmail(), request.getNome(), assunto, corpo);
    }

    @Async
    public boolean processoContato(ContactRequest request) {
        boolean adminOk = enviarNotificacaoContato(request);
        boolean usuarioOk = enviarRespostaAutomatica(request);
        return adminOk && usuarioOk;
    }
}
