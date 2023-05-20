package com.example.miniprojet.ui.search;

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
import com.example.miniprojet.Server;
import com.example.miniprojet.adapters.searchResultRecyclerViewAdapter;
import com.example.miniprojet.databinding.FragmentSearchListBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.models.User;
import com.example.miniprojet.models.searchResultItem;
import com.example.miniprojet.ui.account.AccountActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SearchFragment extends Fragment {

        private FragmentSearchListBinding ListBinding;
        private static String SearchTerm;
        /**
         * Mandatory empty constructor for the fragment manager to instantiate the
         * fragment (e.g. upon screen orientation changes).
         */
        public SearchFragment() {
        }




        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {



            ListBinding = FragmentSearchListBinding.inflate(inflater, container, false);
            View root = ListBinding.getRoot();
            List<searchResultItem> itemsList = search(ListBinding);
            searchResultRecyclerViewAdapter adapter = new searchResultRecyclerViewAdapter(itemsList, new RecyclerViewInterface() {
                @Override
                public void onItemClick(int position) {
                        Intent account = new Intent(getContext(), AccountActivity.class);
                        account.putExtra("accountID", itemsList.get(position).getId());
                        startActivity(account);
                }
            });
            ListBinding.list.setAdapter(adapter);

            return root;
        }
    public static String getSearchTerm() {
        return SearchTerm;
    }

    public static void setSearchTerm(String searchTerm) {
        SearchTerm = searchTerm;
    }
    public List<searchResultItem> search(FragmentSearchListBinding binding){
            binding.list.setVisibility(View.GONE);
            binding.textView3.setVisibility(View.GONE);
            binding.prgrs.setVisibility(View.VISIBLE);
            List<searchResultItem> list = new ArrayList<>();


            StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlSearch(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.has("error")){
                            Toast.makeText(getContext(), "search failed. "+res.get("message"), Toast.LENGTH_LONG).show();
                            binding.textView3.setVisibility(View.VISIBLE);
                            binding.list.setVisibility(View.GONE);
                        }
                        else {
                            JSONArray result = res.getJSONArray("result");
                            for (int i=0; i<result.length(); i++){
                                JSONObject obj = result.getJSONObject(i);
                                if (User.getInstance(getContext()).isLoggedin()){
                                    if (obj.getInt("idClient") == User.getInstance(getContext()).getID())
                                        continue;
                                }

                                searchResultItem item = new searchResultItem();
                                item.setTitle(obj.getString("nomClient") +" "+ obj.getString("prenomClient") +" ");
                                item.setSubTitle(obj.getString("email") );
                                item.setId(obj.getInt("idClient"));

                                list.add(item);
                            }
                            if(result.length()>0){
                                binding.textView3.setVisibility(View.GONE);
                                binding.list.setVisibility(View.VISIBLE);
                            }
                            else{
                                binding.textView3.setVisibility(View.VISIBLE);
                                binding.list.setVisibility(View.GONE);
                            }
                        }
                        binding.prgrs.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "search error.", Toast.LENGTH_LONG).show();
                        binding.prgrs.setVisibility(View.GONE);
                        binding.list.setVisibility(View.GONE);
                        binding.textView3.setVisibility(View.VISIBLE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getContext(),"connection error. "+ error.toString(), Toast.LENGTH_SHORT).show();
                    binding.prgrs.setVisibility(View.GONE);
                    binding.list.setVisibility(View.GONE);
                    binding.textView3.setVisibility(View.VISIBLE);
                }
            }) {
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("table","client");
                    params.put("cols","idClient, nomClient, prenomClient, email, numTel");
                    //index for response result array 0->idClient, 1->nomClient, 2->prenomClient, 3->email, 4->numTel
                    params.put("term",SearchTerm);
                    return params;
                }
            } ;

            RequestQueue reqQ = Volley.newRequestQueue(getContext());
            reqQ.add(Sreq);

            return list;
    }
}
