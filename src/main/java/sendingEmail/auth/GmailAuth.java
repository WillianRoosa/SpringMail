package sendingEmail.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GmailAuth {
    private static final String TOKENS_DIR = "tokens";
    private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/gmail.send");
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public static Credential authorize() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        String clientSecretJsonEnv = System.getenv("GOOGLE_CLIENT_SECRET_JSON");
        String storedCredentialJsonEnv = System.getenv("GOOGLE_STORED_CREDENTIAL_JSON");

        System.out.println("🔍 GOOGLE_CLIENT_SECRET_JSON detectado? " + (clientSecretJsonEnv != null));
        System.out.println("🔍 GOOGLE_STORED_CREDENTIAL_JSON detectado? " + (storedCredentialJsonEnv != null));

        // === AMBIENTE DE PRODUÇÃO (Render, Railway, etc) ===
        if (clientSecretJsonEnv != null && storedCredentialJsonEnv != null) {
            System.out.println("✅ Ambiente de produção detectado. Usando variáveis de ambiente.");

            try {
                JsonObject clientSecretObj = JsonParser.parseString(clientSecretJsonEnv).getAsJsonObject();
                JsonObject secretDetails = clientSecretObj.getAsJsonObject(clientSecretObj.keySet().iterator().next());

                String clientId = secretDetails.get("client_id").getAsString();
                String clientSecret = secretDetails.get("client_secret").getAsString();

                JsonObject storedCredentialObj = JsonParser.parseString(storedCredentialJsonEnv).getAsJsonObject();
                String refreshToken = storedCredentialObj.get("refreshToken").getAsString();

                UserCredentials userCredentials = UserCredentials.newBuilder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setRefreshToken(refreshToken)
                        .build();

                // Adapta para o Credential usado pelo Gmail API
                return new Credential((Credential.AccessMethod) new HttpCredentialsAdapter(userCredentials));
            } catch (Exception e) {
                System.err.println("❌ Erro ao interpretar JSON das variáveis de ambiente. Verifique o formato.");
                throw new IOException("Falha ao ler variáveis de ambiente de credenciais.", e);
            }
        }

        // === AMBIENTE LOCAL ===
        System.out.println("✅ Ambiente local detectado. Carregando client_secret.json e tokens locais.");

        GoogleClientSecrets clientSecrets;
        try (FileReader reader = new FileReader("client_secret.json")) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        } catch (IOException e) {
            throw new IOException("Falha ao carregar client_secret.json. Coloque-o na raiz do projeto.", e);
        }

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        )
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIR)))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 Iniciando autorização Gmail API...");

        try {
            Path tokensPath = Path.of(TOKENS_DIR);
            if (!Files.exists(tokensPath)) {
                Files.createDirectories(tokensPath);
            }

            Credential credential = authorize();
            if (credential != null) {
                System.out.println("✅ Credenciais geradas com sucesso!");
            } else {
                System.err.println("❌ Erro: credenciais não foram obtidas.");
            }
        } catch (Exception e) {
            System.err.println("❌ ERRO FATAL durante a autorização: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
