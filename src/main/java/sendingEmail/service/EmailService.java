package sendingEmail.service;

import com.google.api.client.auth.oauth2.Credential;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sendingEmail.auth.GmailAuth;
import sendingEmail.auth.GmailService;
import sendingEmail.config.MailProperties;
import sendingEmail.dto.ContactRequest;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private final String AUTHOTIZED_EMAIL = "willian.dev2025@gmail.com";
    private final MailProperties mailProperties;
    private GmailService gmailService;

    public EmailService(MailProperties mailProperties) {
        this.mailProperties = mailProperties;
    }

    @PostConstruct
    public void init() {
        try {
            Credential credential = GmailAuth.authorize();
            this.gmailService = new GmailService(credential);
            System.out.println("✅ GmailService inicializado com credenciais OAuth.");
        } catch (Exception e) {
            System.err.println("❌ Erro ao inicializar o GmailService. Verifique se as credenciais foram geradas e se client_secret.json está presente na raiz (apenas para a primeira execução). Detalhe: " + e.getMessage());
        }
    }

    private boolean enviar(String remetenteEmail, String destinatarioEmail, String assunto, String corpoHTML) {
        if (gmailService == null) {
            System.err.println("❌ Serviço de e-mail indisponível. Inicialização OAuth falhou.");
            return false;
        }

        if (destinatarioEmail == null || destinatarioEmail.trim().isEmpty()) {
            System.err.println("❌ E-mail do destinatário está vazio.");
            return false;
        }

        try {
            gmailService.sendEmail(remetenteEmail, destinatarioEmail, assunto, corpoHTML);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar e-mail para " + destinatarioEmail);
            e.printStackTrace();
            return false;
        }
    }

    public boolean enviarNotificacaoContato(ContactRequest request) {
        String assunto = "📩 Novo contato do portfólio";
        String corpo = """
                <h2>Nova mensagem recebida</h2>
                <p><b>Nome:</b></p>
                <p><b>Email:</b></p>
                <p><b>WhatsApp:</b></p>
                <p><b>Mensagem:</b></p>
                """.formatted(request.getNome(), request.getEmail(), request.getTelefone(), request.getMensagem());

        return enviar(AUTHOTIZED_EMAIL, mailProperties.getAdminEmail(), assunto, corpo);
    }

    public boolean enviarRespostaAutomatica(ContactRequest request) {
        String assunto = "✅ Recebemos sua mensagem!";
        String corpo = """
                <h2>Olá %s,</h2>
                <p>Obrigado por entrar em contato! Recebemos sua mensagem e responderemos em breve.</p>
                <p>Atenciosamente,<br><b>Willian Rosa - Developer</b></p>
                """.formatted(request.getNome());

        return enviar(AUTHOTIZED_EMAIL, request.getEmail(), assunto, corpo);
    }

    @Async
    public CompletableFuture<Boolean> processoContato(ContactRequest request) {
        boolean adminOk = enviarNotificacaoContato(request);
        boolean usuarioOk = enviarRespostaAutomatica(request);

        boolean sucesso = adminOk && usuarioOk;
        return CompletableFuture.completedFuture(sucesso);
    }
}
