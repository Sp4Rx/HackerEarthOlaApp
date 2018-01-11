package io.github.sp4rx.hackereartholaapp.networking;

/**
 * Created by suvajit.<br>
 * Custom network response listener to handle success and failure of network request
 */

public interface VolleyResponseListener {
    void onResponse(String response);

    void onError(String message);

}