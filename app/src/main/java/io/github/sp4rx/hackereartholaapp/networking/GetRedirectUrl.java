package io.github.sp4rx.hackereartholaapp.networking;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by suvajit.
 */


public class GetRedirectUrl extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                return urlConnection.getHeaderField("Location");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

