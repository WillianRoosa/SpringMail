package com.willian.springmail.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.util.List;

public class GmailAuth {
    public static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/gmail.send");

    private static Credential cachedCredential;
    private static final Object lock = new Object();

    private static void log(String msg) {
        System.out.println("[GMAIL-AUTH] " + msg);
    }

    private static void error(String msg, Exception e) {
        System.err.println("[GMAIL_AUTH]   [ERROR] " + msg);
        if (e != null) e.printStackTrace();
    }

    public static Credential authorize() throws Exception {
        synchronized (lock) {
            if (cachedCredential != null && isTokenValid()) {
                log("Usando token em cache - ainda válido.");
                return cachedCredential;
            }
            cachedCredential = buildCredential();
            return cachedCredential;
        }
    }

    private static boolean isTokenValid() {
        if (cachedCredential.getAccessToken() == null) return false;
        Long expiresIn = cachedCredential.getExpiresInSeconds();
        return expiresIn != null && expiresIn > 300;
    }

    private static Credential buildCredential() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        String refreshToken = System.getenv("GOOGLE_GMAIL_REFRESH_TOKEN");

        if (clientId == null || clientSecret == null || refreshToken == null) {
            throw new IllegalStateException("Variáveis de ambiente OAuth ausentes!\n" + "Verifique [GOOGLE_CLIENT_ID] - [GOOGLE_CLIENT_SECRET] - [GOOGLE_GMAIL_REFRESH_TOKEN]");
        }

        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setInstalled(details);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setAccessType("Offiline")
                .build();

        Credential credential = flow.createAndStoreCredential(
                new TokenResponse().setRefreshToken(refreshToken), "user");

        boolean refreshed = credential.refreshToken();
        if (!refreshed) {
            throw new IllegalStateException("Refresh token inválido ou expirado. \n"
                    + "Gere um novo token via OAuth Playground e atualize [GOOGLE_GMAIL_REFRESH_TOKEN].");
        }

        log("✅ Token OAuth renovado com sucesso!");
        return credential;
    }
}
