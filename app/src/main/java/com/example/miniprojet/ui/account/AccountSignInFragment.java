package com.example.miniprojet.ui.account;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.miniprojet.Server;
import com.example.miniprojet.MainActivity;
import com.example.miniprojet.databinding.FragmentAccountSignInBinding;

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
        if(ident.contains("@")) email = ident;
        else numTel = ident;

        String finalEmail = email;
        ProgressDialog prgrs = new ProgressDialog(getContext());
        prgrs.setMessage("login in progress...");
        prgrs.show();
        String finalNumTel = numTel;
        StringRequest req = new StringRequest(Request.Method.POST, dbc.getUrlLogin(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);

                            if(res.has("error")){
                                Toast.makeText(getContext(),"login failed. "+res.getString("message"), Toast.LENGTH_LONG).show();
                                prgrs.dismiss();
                            }

                            else{
                                User user = User.getInstance(getContext());
                                user.userLogin(
                                        res.getInt("idClient"),
                                        res.getString("email").toLowerCase(),
                                        res.getString("numTel"),
                                        res.getString("nomClient"),
                                        res.getString("prenomClient"),
                                        res.getInt("ageClient")
                                );
                                if(user.isLoggedin()) {
                                    Toast.makeText(getContext(), "login successful.", Toast.LENGTH_SHORT).show();
                                    prgrs.dismiss();
                                    Intent i = new Intent(getContext(), MainActivity.class);
                                    MainActivity.setKeepConnected(true);
                                    startActivity(i);
                                    getActivity().finish();

                                }
                                else{
                                    Toast.makeText(getContext(),"login failed, try again.", Toast.LENGTH_LONG).show();
                                    prgrs.dismiss();

                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(),"an error occurred.", Toast.LENGTH_SHORT).show();
                            prgrs.dismiss();

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),"connection error.", Toast.LENGTH_SHORT).show();
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