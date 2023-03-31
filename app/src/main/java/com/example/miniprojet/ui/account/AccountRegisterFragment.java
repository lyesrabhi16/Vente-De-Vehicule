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
import com.example.miniprojet.DBConnection;
import com.example.miniprojet.MainActivity;
import com.example.miniprojet.R;
import com.example.miniprojet.databinding.FragmentAccountBinding;
import com.example.miniprojet.databinding.FragmentAccountRegisterBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class AccountRegisterFragment extends Fragment {

    private FragmentAccountRegisterBinding RegBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RegBinding = FragmentAccountRegisterBinding.inflate(inflater, container, false);
        View root = RegBinding.getRoot();

        RegBinding.buttonResgister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nomClient = RegBinding.edittextNomClient.getText().toString().trim();
                String prenomClient = RegBinding.edittextPrenomClient.getText().toString().trim();
                String ageClient = RegBinding.edittextAgeClient.getText().toString().trim();
                String email = RegBinding.edittextEmail.getText().toString().trim();
                String numTel = RegBinding.edittextNumTel.getText().toString().trim();
                String password = RegBinding.edittextPassword.getText().toString().trim();
                String password2 = RegBinding.edittextPassword.getText().toString().trim();

                if (!password.equals(password2)){
                    Toast.makeText(getContext(), "Please re-enter password correctly", Toast.LENGTH_SHORT).show();
                    return;
                }
                register(
                        nomClient,
                        prenomClient,
                        ageClient,
                        email,
                        numTel,
                        password
                        );
            }
        });

        return root;
    }

    public void register(String nomClient, String prenomClient, String ageClient, String email, String numTel, String password) {
        ProgressDialog prgrs = new ProgressDialog(getContext());
        prgrs.setMessage("registering...");
        prgrs.show();

        StringRequest str_req = new StringRequest(Request.Method.POST, DBConnection.getUrlRegister(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject res = new JSONObject(response);

                    if(res.has("error")){
                        Toast.makeText(getContext(),"registration failed."+ res.getString("message"), Toast.LENGTH_LONG).show();
                        prgrs.dismiss();
                    }
                    else {
                        User user = User.getInstance(getContext());
                        user.userLogin(
                                res.getInt("idClient"),
                                res.getString("email"),
                                res.getString("numTel"),
                                res.getString("nomClient")
                        );
                        if (user.isLoggedin()){
                            Toast.makeText(getContext(), "registration successful.", Toast.LENGTH_SHORT).show();
                            prgrs.dismiss();
                            Intent i = new Intent(getContext(), MainActivity.class);
                            startActivity(i);
                            getActivity().finish();
                        }
                        else {
                            Toast.makeText(getContext(), "registration failed.", Toast.LENGTH_SHORT).show();
                            prgrs.dismiss();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "registration error.", Toast.LENGTH_SHORT).show();
                    prgrs.dismiss();
                }

            }
        }, new Response.ErrorListener() {
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
                params.put("nomClient",nomClient);
                params.put("prenomClient",prenomClient);
                params.put("ageClient",ageClient);
                params.put("email",email);
                params.put("numTel",numTel);
                params.put("password",password);
                return params;

            }
        };

        RequestQueue reqQ = Volley.newRequestQueue(getContext());
        reqQ.add(str_req);

        }




}

