package io.github.sp4rx.hackereartholaapp.networking;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by suvajit.<br>
 * This is a custom network wrapper class.
 */

public class VolleyUtils {

    /**
     * Handle GET network requests
     *
     * @param context  Context
     * @param url      API Url
     * @param listener {@link VolleyResponseListener}
     */
    public static void GET_METHOD(Context context, String url, final VolleyResponseListener listener) {

        // Initialize a new StringRequest
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        listener.onResponse(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error.toString());

                    }
                })

        {


        };

        // Access the RequestQueue through singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    /**
     * Handle POST network requests
     *
     * @param context   Context
     * @param url       API Url
     * @param getParams POST parameters
     * @param listener  {@link VolleyResponseListener}
     */
    public static void POST_METHOD(Context context, String url, final Map<String, String> getParams, final VolleyResponseListener listener) {

        // Initialize a new StringRequest
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        listener.onResponse(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error.toString());

                    }
                })

        {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                getParams.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }

        };

        // Access the RequestQueue through singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}