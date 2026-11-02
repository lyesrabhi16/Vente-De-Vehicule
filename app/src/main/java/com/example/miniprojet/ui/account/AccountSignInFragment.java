package com.example.miniprojet.ui.account;

import com.example.miniprojet.AuthRequest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.miniprojet.MainActivity;
import com.example.miniprojet.Server;
import com.example.miniprojet.databinding.FragmentAccountSignInBinding;
import com.example.miniprojet.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AccountSignInFragment extends Fragment {

    private FragmentAccountSignInBinding SignInbinding;
    private Server dbc;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SignInbinding = FragmentAccountSignInBinding.inflate(inflater, container, false);
        View root = SignInbinding.getRoot();

        SignInbinding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        return root;
    }

    public void login(){
        String ident = SignInbinding.edittextId.getText().toString().trim();
        String pass = SignInbinding.edittextPassword.getText().toString().trim();
        String email = "";
        String numTel = "";
        if(ident.contains("@") || (ident.startsWith("$") && ident.endsWith("$"))) email = ident;
        else numTel = ident;

        String finalEmail = email;
        ProgressDialog prgrs = new ProgressDialog(getContext());
        prgrs.setMessage("Connexion…");
        prgrs.show();
        String finalNumTel = numTel;
        StringRequest req = new AuthRequest(Request.Method.POST, dbc.getUrlLogin(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            if(res.has("error")){
                                Toast.makeText(getContext(),"Échec de la connexion. "+res.getString("error"), Toast.LENGTH_LONG).show();
                                prgrs.dismiss();
                                return;
                            }
                            if(res.has("ADMIN")){
                                if(res.getBoolean("ADMIN")){
                                    User user = User.getInstance(getContext());
                                    user.setToken(res.optString("token", null));
                                    user.userLogin(
                                            res.getInt("idClient"),
                                            res.getString("email").toLowerCase(),
                                            res.getString("numTel"),
                                            res.getString("nomClient"),
                                            res.getString("prenomClient"),
                                            res.getInt("ageClient"),
                                            true
                                    );
                                    if(user.isLoggedin()) {
                                        Toast.makeText(getContext(), "Connexion réussie.", Toast.LENGTH_SHORT).show();
                                        prgrs.dismiss();
                                        Intent i = new Intent(getContext(), MainActivity.class);
                                        MainActivity.setKeepConnected(true);
                                        startActivity(i);
                                        getActivity().finish();

                                    }
                                    else{
                                        Toast.makeText(getContext(),"Échec de la connexion, réessayez.", Toast.LENGTH_LONG).show();
                                        prgrs.dismiss();

                                    }
                                }

                            }
                            else{
                                User user = User.getInstance(getContext());
                                user.setToken(res.optString("token", null));
                                user.userLogin(
                                        res.getInt("idClient"),
                                        res.getString("email").toLowerCase(),
                                        res.getString("numTel"),
                                        res.getString("nomClient"),
                                        res.getString("prenomClient"),
                                        res.getInt("ageClient"),
                                        false
                                );
                                if(user.isLoggedin()) {
                                    Toast.makeText(getContext(), "Connexion réussie.", Toast.LENGTH_SHORT).show();
                                    prgrs.dismiss();
                                    Intent i = new Intent(getContext(), MainActivity.class);
                                    MainActivity.setKeepConnected(true);
                                    startActivity(i);
                                    getActivity().finish();

                                }
                                else{
                                    Toast.makeText(getContext(),"Échec de la connexion, réessayez.", Toast.LENGTH_LONG).show();
                                    prgrs.dismiss();

                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(),"Une erreur est survenue. "+e, Toast.LENGTH_SHORT).show();
                            prgrs.dismiss();

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),"Erreur de connexion.", Toast.LENGTH_SHORT).show();
                        prgrs.dismiss();
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String,String>();
                if(finalEmail != "") params.put("email", finalEmail);
                else params.put("numTel", finalNumTel);
                params.put("password", pass);
                return params;

            }

            @Override
            public Priority getPriority() {
                return Priority.HIGH;
            }
        };

        RequestQueue reqQ = Volley.newRequestQueue(getContext());
        reqQ.add(req);
    }

}