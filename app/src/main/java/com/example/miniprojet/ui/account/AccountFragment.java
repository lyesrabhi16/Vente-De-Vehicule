package com.example.miniprojet.ui.account;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.miniprojet.DBConnection;
import com.example.miniprojet.MainActivity;
import com.example.miniprojet.R;
import com.example.miniprojet.databinding.ActivityMainBinding;
import com.example.miniprojet.databinding.FragmentAccountBinding;
import com.example.miniprojet.databinding.FragmentAccountSignInBinding;
import com.example.miniprojet.databinding.FragmentDashboardBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    private FragmentAccountSignInBinding SignInbinding;
    private FragmentAccountBinding AccountBinding;
    private ActivityMainBinding MainBinding;
    private DBConnection dbc;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root;
        User user = User.getInstance(getContext());
        Handler handlerUI = new Handler();
        if ( user.isLoggedin() ){
            AccountBinding = FragmentAccountBinding.inflate(inflater, container, false);
            root = AccountBinding.getRoot();

            AccountBinding.buttonLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        logout();
                        Intent i = new Intent(getContext(), MainActivity.class);
                        startActivity(i);
                }
            });

        }
        else {
            SignInbinding = FragmentAccountSignInBinding.inflate(inflater, container, false);
            root = SignInbinding.getRoot();
            SignInbinding.buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    login();

                }
            });
        }

        return root;

    }

    public void login(){
        String ident = SignInbinding.edittextId.getText().toString().trim();
        String pass = SignInbinding.edittextPassword.getText().toString().trim();
        String email = "";
        String numTel = "";
        boolean success;
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
                            }

                            else{
                                User user = User.getInstance(getContext());
                                user.userLogin(
                                        res.getInt("idClient"),
                                        res.getString("email").toLowerCase(),
                                        res.getString("numTel"),
                                        res.getString("nomClient")
                                );
                                if(user.isLoggedin()) {
                                    Toast.makeText(getContext(), "login successful.", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(getContext(), MainActivity.class);
                                    startActivity(i);
                                }
                                else{
                                    Toast.makeText(getContext(),"login failed, try again.", Toast.LENGTH_LONG).show();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(),"an error occurred.", Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        prgrs.dismiss();
                        Toast.makeText(getContext(),"login failed.", Toast.LENGTH_SHORT).show();
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
        prgrs.dismiss();



    }

    public void logout(){
        ProgressDialog prgrs = new ProgressDialog(getContext());
        prgrs.setMessage("Logout in progress...");
        User user = User.getInstance(getContext());
        user.logout();
        if(!user.isLoggedin())
            Toast.makeText(getContext(),"logout successful.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(),"logout failed.", Toast.LENGTH_LONG).show();
        prgrs.dismiss();

    }

}
