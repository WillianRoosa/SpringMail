package sendingEmail.auth;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TokenExtractor {
    public static void main(String[] args) throws Exception {
        File tokenDir = new File("tokens");
        var dataStore = new FileDataStoreFactory(tokenDir).getDataStore("StoredCredential");

        StoredCredential stored = (StoredCredential) dataStore.get("user");

        if (stored == null) {
            System.err.println("❌ Nenhum token encontrado. Execute GmailAuth.main() primeiro.");
            return;
        }

        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("accessToken", stored.getAccessToken() != null ? stored.getAccessToken() : "null");
        tokenData.put("refreshToken", stored.getRefreshToken() != null ? stored.getRefreshToken() : "null");
        tokenData.put("tokenType", "Bearer");
        tokenData.put("scope", "https://www.googleapis.com/auth/gmail.send");

        String json = new Gson().toJson(tokenData);

        System.out.println("\n✅ Copie este JSON e use no Render:\n\n" + json);
    }
}
