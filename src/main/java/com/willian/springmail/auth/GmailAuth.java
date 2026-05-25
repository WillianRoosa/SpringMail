package com.willian.springmail.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;


import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GmailAuth {
    private static final String TOKENS_DIR = "tokens";
    private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/gmail.send");
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static void log(String msg) {
        System.out.println("[GMAIL-AUTH] " + msg);
    }

    private static void error(String msg, Exception e) {
        System.err.println("[GMAIL-AUTH] [ERROR] " + msg);
        if (e != null) {
            e.printStackTrace();
        }
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Variável de ambiente não encontrada: " + name);
        }
        return value;
    }

    public static Credential authorize() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        String profile = System.getenv("SPRING_PROFILES_ACTIVE");

        if ("prod".equalsIgnoreCase(profile)) {
            log("Detectado ambiente PROD. Carregando credenciais das variáveis de ambiente.");

            String clientId = System.getenv("GOOGLE_CLIENT_ID");
            String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
            String refreshToken = System.getenv("GOOGLE_GMAIL_REFRESH_TOKEN");

            try {
                GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
                details.setClientId(clientId);
                details.setClientSecret(clientSecret);

                GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setInstalled(details);

                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport,
                        JSON_FACTORY,
                        clientSecrets,
                        SCOPES
                )
                        .setAccessType("offline")
                        .build();

                Credential credential = flow.createAndStoreCredential(new TokenResponse().setRefreshToken(refreshToken),
                        "user");

                boolean refreshed = credential.refreshToken();
                if (!refreshed) {
                    throw new IllegalArgumentException("Falha ao renovar o acess token...");
                }

                log("Token OAuth renovado com sucesso em ambiente PROD!");
                return credential;
            } catch (Exception e) {
                error("Erro ao inicializar as credenciais Gmail em PROD", e);
                throw e;
            }
        }

        log("Detectado ambiente LOCAL. Carregando credenciais de arquivos (client_secret.json e /tokens).");

        GoogleClientSecrets clientSecrets;
        try (FileReader reader = new FileReader("client_secret.json")) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
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
        log("Iniciando Autorização Local API Gmail.");

        try {
            Path tokensPath = Path.of(TOKENS_DIR);
            if (!Files.exists(tokensPath)) {
                Files.createDirectories(tokensPath);
            }

            Credential credential = authorize();

            if (credential != null && credential.getAccessToken() != null) {
                log("SUCESSO! Token gerado e salvo em: " + TOKENS_DIR + "/StoredCredential");
                log("O refresh token permitirá o uso contínuo.");
            } else {
                error("\nERRO: Credencial não foi gerada corretamente.", null);
            }
        } catch (Exception e) {
            error("\nERRO FATAL durante a autorização: ", e);
        }
    }
}
