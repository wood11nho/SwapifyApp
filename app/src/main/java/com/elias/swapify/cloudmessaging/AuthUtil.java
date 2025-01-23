package com.elias.swapify.cloudmessaging;

import android.content.Context;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class AuthUtil {

    private static final String SCOPES = "https://www.googleapis.com/auth/firebase.messaging";

    public static String getAccessToken(Context context) throws IOException {
        InputStream inputStream = context.getAssets().open("cloud_messaging/swapify-e426d-c98177abf68d.json");
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(inputStream)
                .createScoped(Collections.singletonList(SCOPES));
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }
}
