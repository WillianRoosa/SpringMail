package sendingEmail.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
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

        if (clientSecretJsonEnv != null && storedCredentialJsonEnv != null) {
            System.out.println("✅ Detectado ambiente de produção. Carregando credenciais das variáveis de ambiente.");

            try {
                JsonObject clientSecretObj = JsonParser.parseString(clientSecretJsonEnv).getAsJsonObject();
                JsonObject secretDetails = clientSecretObj.getAsJsonObject(clientSecretObj.keySet().iterator().next());

                String clientId = secretDetails.get("client_id").getAsString();
                String clientSecret = secretDetails.get("client_secret").getAsString();

                JsonObject storedCredentialObj = JsonParser.parseString(storedCredentialJsonEnv).getAsJsonObject();
                String refreshToken = storedCredentialObj.get("refreshToken").getAsString();

                return new GoogleCredential.Builder()
                        .setTransport(httpTransport)
                        .setJsonFactory(JSON_FACTORY)
                        .setClientSecrets(clientId, clientSecret)
                        .build()
                        .setRefreshToken(refreshToken);
            } catch (Exception e) {
                System.err.println("❌ Erro fatal ao parsear JSON das variáveis de ambiente. Verifique se elas foram copiadas corretamente.");
                throw new IOException("Falha ao ler variáveis de ambiente de credenciais.", e);
            }
        } else {
            System.out.println("✅ Detectado ambiente local. Carregando credenciais de arquivos (client_secret.json e /tokens).");

            GoogleClientSecrets clientSecrets;
            try (FileReader reader = new FileReader("client_secret.json")) {
                clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
            } catch (IOException e) {
                throw new IOException("Falha ao carregar client_secret.json. Certifique-se de que ele está na raiz do projeto.", e);
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
