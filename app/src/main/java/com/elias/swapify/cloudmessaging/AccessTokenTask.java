package com.elias.swapify.cloudmessaging;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

public class AccessTokenTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private AccessTokenListener listener;

    public AccessTokenTask(Context context, AccessTokenListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return AuthUtil.getAccessToken(context);
        } catch (IOException e) {
            Log.e("AccessTokenTask", "Failed to get access token", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String accessToken) {
        if (listener != null) {
            listener.onAccessTokenReceived(accessToken);
        }
    }

    public interface AccessTokenListener {
        void onAccessTokenReceived(String accessToken);
    }
}
