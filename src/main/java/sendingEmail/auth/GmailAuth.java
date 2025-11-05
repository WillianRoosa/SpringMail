package sendingEmail.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GmailAuth {
    private static final String TOKENS_DIR = "tokens";
    private static final String CLIENT_SECRET_FILE = "client_secret.json";
    private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/gmail.send");
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public static Credential authorize() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleClientSecrets clientSecrets;
        try (InputStreamReader reader = new FileReader(CLIENT_SECRET_FILE)) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao carregar " + CLIENT_SECRET_FILE + ". Certifique-se de que ele está na raiz do projeto.", e);
        }

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        )
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIR)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Iniciando Autorização API Gmail.");

        try {
            Path tokensPath = Path.of(TOKENS_DIR);
            if (!Files.exists(tokensPath)) {
                Files.createDirectories(tokensPath);
            }

            Credential credential = authorize();
            if (credential != null && credential.getAccessToken() != null) {
                System.out.println("✅ SUCESSO! Token gerado e salvo em: " + TOKENS_DIR + "/StoredCredential");
                System.out.println("O refresh token permitirá o uso contínuo.");
            } else {
                System.err.println("\n❌ ERRO: Credencial não foi gerada corretamente.");
            }
        } catch (Exception e) {
            System.err.println("\n❌ ERRO FATAL durante a autorização: " + e.getMessage());
            System.err.println("Verifique se o arquivo client_secret.json está na raiz e se o projeto está configurado corretamente no Google Cloud.");
        }
    }
}
