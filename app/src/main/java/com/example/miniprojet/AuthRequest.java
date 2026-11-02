package com.example.miniprojet;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.miniprojet.models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Une requête vers le serveur : elle présente le jeton de connexion (en-tête Authorization: Bearer), et si le serveur le
 * refuse (HTTP 401 : jeton expiré ou compte supprimé) elle déconnecte l'utilisateur au lieu de laisser chaque écran échouer
 * à sa façon.
 */
public class AuthRequest extends StringRequest {

    public AuthRequest(int method, String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, listener, expiring(errorListener));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>(super.getHeaders());
        String token = User.getInstance(MiniProjetApp.context()).getToken();
        if (token != null) {
            headers.put("Authorization", "Bearer " + token);
        }
        return headers;
    }

    private static Response.ErrorListener expiring(@Nullable Response.ErrorListener next) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    User user = User.getInstance(MiniProjetApp.context());
                    if (user.getToken() != null) {
                        user.logout();
                        SocketClient.closeSocket();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MiniProjetApp.context(), "Session expirée, reconnectez-vous.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                if (next != null) {
                    next.onErrorResponse(error);
                }
            }
        };
    }
}
