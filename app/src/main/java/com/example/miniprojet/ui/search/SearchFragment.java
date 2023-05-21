package com.example.miniprojet.ui.search;

import android.content.Context;
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
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.Client;
import com.example.miniprojet.models.PersonItem;
import com.example.miniprojet.models.User;
import com.example.miniprojet.models.searchResultItem;
import com.example.miniprojet.ui.account.AccountActivity;
import com.example.miniprojet.ui.annonce.AnnonceActivity;

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
        private ArrayList<searchResultItem> itemsList;
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

            itemsList = new ArrayList<searchResultItem>();
            searchResultRecyclerViewAdapter adapter = new searchResultRecyclerViewAdapter(itemsList, new RecyclerViewInterface() {
                @Override
                public void onItemClick(int position) {
                        searchResultItem item = itemsList.get(position);
                        if(item.getType() == searchResultItem.RESULT_PERSON){
                            Intent account = new Intent(getContext(), AccountActivity.class);
                            account.putExtra("accountID", ((searchResultItem)itemsList.get(position)).getPerson().getUserID());
                            startActivity(account);
                        } else if (item.getType() == searchResultItem.RESULT_ANNONCE) {
                            Intent annonce = new Intent(getContext(), AnnonceActivity.class);
                            annonce.putExtra("idAnnonce", item.getAnnonce().getIdAnnonce());
                            startActivity(annonce);
                        }
                }
            });
            ListBinding.list.setAdapter(adapter);

            ListBinding.list.setVisibility(View.GONE);
            ListBinding.textView3.setVisibility(View.GONE);
            ListBinding.prgrs.setVisibility(View.VISIBLE);
            search("annonce", "idAnnonce,titre,description,typeVehicule,marqueVehicule,modeleVehicule, couleurVehicule,transmissionVehicule,kilometrageVehicule,anneeVehicule,moteurVehicule,energieVehicule,prixVehicule,idClient", getContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    if(ListBinding.prgrs != null)
                        ListBinding.prgrs.setVisibility(View.GONE);
                    itemsList = (ArrayList<searchResultItem>) args;
                    if(itemsList.size()>0){
                        ListBinding.textView3.setVisibility(View.GONE);
                        ListBinding.list.setVisibility(View.VISIBLE);
                        adapter.setItemList(itemsList);
                        ListBinding.list.setAdapter(adapter);

                        search("client", "idClient, nomClient, prenomClient, email", getContext(), new RequestFinished() {
                            @Override
                            public void onFinish(ArrayList args) {
                                itemsList.addAll((ArrayList<searchResultItem>) args);
                                adapter.setItemList(itemsList);
                                ListBinding.list.setAdapter(adapter);
                            }

                            @Override
                            public void onError(ArrayList args) {

                            }
                        });

                    }
                    else{
                        ListBinding.textView3.setVisibility(View.VISIBLE);
                        ListBinding.list.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onError(ArrayList args) {
                    if(ListBinding.prgrs != null)
                        ListBinding.prgrs.setVisibility(View.GONE);
                    ListBinding.textView3.setVisibility(View.VISIBLE);
                    ListBinding.list.setVisibility(View.GONE);
                }
            });


            return root;
        }
    public static String getSearchTerm() {
        return SearchTerm;
    }

    public static void setSearchTerm(String searchTerm) {
        SearchTerm = searchTerm;
    }
    public static void  search(String table, String cols, Context ctx, RequestFinished Req){

            List<searchResultItem> list = new ArrayList<>();


            StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlSearch(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.has("error")){
                            Toast.makeText(ctx, "search failed. "+res.get("error"), Toast.LENGTH_LONG).show();
                            ArrayList l = new ArrayList();
                            l.add(res.get("error"));
                            Req.onError(l);

                        }
                        else {
                            JSONArray result = res.getJSONArray("result");
                            for (int i=0; i<result.length(); i++){
                                JSONObject obj = result.getJSONObject(i);
                                if (User.getInstance(ctx).isLoggedin()){
                                    if (obj.getInt("idClient") == User.getInstance(ctx).getID())
                                        continue;
                                }
                                searchResultItem item = new searchResultItem();
                                if(obj.has("idAnnonce")){
                                    Annonce annonce = new Annonce();
                                    annonce.setIdAnnonce(obj.getInt("idAnnonce"));
                                    annonce.setTitle(obj.getString("titre"));
                                    annonce.setDesc(obj.getString("description"));
                                    annonce.setType(obj.getString("typeVehicule"));
                                    annonce.setMarque(obj.getString("marqueVehicule"));
                                    annonce.setModele(obj.getString("modeleVehicule"));
                                    annonce.setCouleur(obj.getString("couleurVehicule"));
                                    annonce.setTransmission(obj.getString("transmissionVehicule"));
                                    annonce.setKilometrage(obj.getInt("kilometrageVehicule"));
                                    annonce.setAnnee(obj.getInt("anneeVehicule"));
                                    annonce.setMoteur(obj.getString("moteurVehicule"));
                                    annonce.setEnergie(obj.getString("energieVehicule"));
                                    annonce.setPrix(obj.getString("prixVehicule"));
                                    annonce.setIdUser(obj.getInt("idClient"));
                                    Client.getClient(annonce.getIdUser(), ctx, new RequestFinished() {
                                        @Override
                                        public void onFinish(ArrayList args) {
                                            Client c = (Client) args.get(0);
                                            annonce.setUserTitle(c.getNom()+" "+c.getPrenom()+" ");
                                            annonce.setUserSubTitle(c.getEmail());
                                            item.setAnnonce(annonce);
                                            item.setType(searchResultItem.RESULT_ANNONCE);
                                            list.add(item);
                                            Req.onFinish((ArrayList) list);
                                        }

                                        @Override
                                        public void onError(ArrayList args) {

                                        }
                                    });
                                }
                                else{
                                    PersonItem p = new PersonItem();
                                    p.setTitle(obj.getString("nomClient") +" "+ obj.getString("prenomClient") +" ");
                                    p.setSubTitle(obj.getString("email") );
                                    p.setUserID(obj.getInt("idClient"));
                                    item.setPerson(p);
                                    item.setType(searchResultItem.RESULT_PERSON);
                                    list.add(item);
                                }
                            }
                            Req.onFinish((ArrayList) list);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ctx, "search error.", Toast.LENGTH_LONG).show();
                        ArrayList l = new ArrayList();
                        l.add(e);
                        Req.onError(l);

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ctx,"connection error. "+ error.toString(), Toast.LENGTH_SHORT).show();
                    ArrayList l = new ArrayList();
                    l.add(error);
                    Req.onError(l);
                }
            }) {
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("table",table);
                    params.put("cols",cols);
                    //index for response result array 0->idClient, 1->nomClient, 2->prenomClient, 3->email, 4->numTel
                    params.put("term",SearchTerm);
                    return params;
                }
            } ;

            RequestQueue reqQ = Volley.newRequestQueue(ctx);
            reqQ.add(Sreq);

    }
}
