package com.example.miniprojet.ui.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.miniprojet.MainActivity;
import com.example.miniprojet.R;
import com.example.miniprojet.Server;
import com.example.miniprojet.SocketClient;
import com.example.miniprojet.databinding.FragmentAccountBinding;
import com.example.miniprojet.databinding.FragmentAdminPanelBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Client;
import com.example.miniprojet.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class AdminPanelFragment extends Fragment {


    private FragmentAdminPanelBinding binding;
    private FragmentAccountBinding Abinding;
    private ArrayList changed;


    public AdminPanelFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentAdminPanelBinding.inflate(getLayoutInflater());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAdminPanelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Abinding = FragmentAccountBinding.bind(binding.containerAccount.getRoot());

        User user = User.getInstance(getContext());

        Abinding.idTextLayout.setVisibility(View.VISIBLE);
        Abinding.idTextLayout.setEnabled(false);
        Abinding.id.setText(user.getID()+"");
        Abinding.nom.setText(user.getNom());
        Abinding.prenom.setText(user.getPrenom());
        Abinding.age.setText(user.getAge()+"");
        Abinding.email.setText(user.getEmail());
        Abinding.numTel.setText(user.getNumtel());
        Abinding.buttonDeleteAccount.setVisibility(View.VISIBLE);
        Abinding.buttonChangePassword.setVisibility(View.VISIBLE);
        Abinding.buttonLogout.setVisibility(View.VISIBLE);

        //get image
        if(user.getImageName() != null){
            Bitmap avatar = Server.getBitmap(getContext(), user.getImageName());
            if (avatar != null){
                Abinding.userAvatar.setImageBitmap(avatar);
            }
        }
        else{
            Server.getImage("imageClient-[" + user.getID() + "].jpeg", getContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    Bitmap img = (Bitmap) args.get(0);
                    user.setImageName(Server.saveBitmap(getContext(), "imageClient", img));
                    Abinding.userAvatar.setImageBitmap(img);
                }

                @Override
                public void onError(ArrayList args) {

                }
            });
        }

        //pick and set image
        ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        try {
                            Bitmap img = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                            user.setImageName(Server.saveBitmap(getContext(), "imageClient", img));
                            JSONObject o = new JSONObject();
                            o.put("imgB64", Server.ImageToBase64(img, getContext()));
                            o.put("id", user.getID());
                            o.put("format", "jpeg");
                            o.put("type", Server.TYPE_IMAGE_AVATAR);
                            Server.sendImageToserver(o, getContext());
                            Abinding.userAvatar.setImageURI(selectedImageUri);
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "Error : "+e, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "Error : "+e, Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }
        });
        Abinding.userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(gallery);
            }
        });

        //show save button if info was changed
        changed = new ArrayList();
        Abinding.id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ShowOrHideSave_Manager( String.join("", charSequence), user.getID()+"",Abinding.id.getId());

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ShowOrHideSave_Manager(editable.toString(), user.getID()+"",Abinding.id.getId());

            }
        });
        Abinding.nom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ShowOrHideSave_Manager( String.join("", charSequence), user.getNom(),Abinding.nom.getId());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ShowOrHideSave_Manager(editable.toString(), user.getNom(),Abinding.nom.getId());

            }
        });
        Abinding.prenom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ShowOrHideSave_Manager(String.join("", charSequence), user.getPrenom(),Abinding.prenom.getId());

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ShowOrHideSave_Manager(editable.toString(), user.getPrenom(),Abinding.prenom.getId());

            }
        });
        Abinding.age.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ShowOrHideSave_Manager(String.join("", charSequence), user.getAge()+"",Abinding.age.getId());

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ShowOrHideSave_Manager(editable.toString(), user.getAge()+"",Abinding.age.getId());

            }
        });
        Abinding.email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().startsWith("$")){
                    Abinding.email.setText("$"+editable.toString());
                }
                if(!editable.toString().endsWith("$")){
                    Abinding.email.setText(editable.toString()+"$");
                }
                ShowOrHideSave_Manager(editable.toString(), user.getEmail(),Abinding.email.getId());

            }
        });
        Abinding.numTel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ShowOrHideSave_Manager(String.join("", charSequence), user.getNumtel(),Abinding.numTel.getId());

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ShowOrHideSave_Manager(editable.toString(), user.getNumtel(),Abinding.numTel.getId());
            }
        });


        Abinding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lastname = Abinding.nom.getText().toString().trim();
                String firstname = Abinding.prenom.getText().toString().trim();
                String age = Abinding.age.getText().toString().trim();
                String email = Abinding.email.getText().toString().trim();
                String phonenumber = Abinding.numTel.getText().toString().trim();
                if(firstname.equals("")) firstname="null";
                if(age.equals(""))age = "0";
                if(phonenumber.equals("")) phonenumber="null";
                Client client = new Client(
                        user.getID(),
                        Integer.parseInt(age),
                        email,
                        phonenumber,
                        lastname,
                        firstname
                );
                ProgressDialog prgs = new ProgressDialog(getActivity());
                prgs.setMessage("Updating...");
                prgs.show();
                Client.updateClient(client, getContext(), new RequestFinished() {
                    @Override
                    public void onFinish(ArrayList args) {
                        if(Abinding == null) return;
                        prgs.dismiss();
                        user.logout();
                        user.userLogin(
                                client.getIdClient(),
                                client.getEmail(),
                                client.getNumTel(),
                                client.getNom(),
                                client.getPrenom(),
                                client.getAge(),
                                true
                        );
                        Abinding.buttonSave.setVisibility(View.GONE);

                    }

                    @Override
                    public void onError(ArrayList args) {
                        if(Abinding == null) return;
                        prgs.dismiss();
                    }
                });
            }
        });
        Abinding.buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "to be implemented later.", Toast.LENGTH_SHORT).show();
            }
        });
        Abinding.buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setMessage("Are you sure you want to delete your account ?");
                alert.setTitle(R.string.delete_account);
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        ProgressDialog prgrs = new ProgressDialog(getActivity());
                        prgrs.setMessage("Deleting Account...");
                        prgrs.show();
                        Client.delClient(user.getID(), getContext(), new RequestFinished() {
                            @Override
                            public void onFinish(ArrayList args) {
                                prgrs.dismiss();
                                if(((String)args.get(0)).equals("success")){
                                    logout();
                                    SocketClient.closeSocket();
                                    Intent i = new Intent(getContext(), MainActivity.class);
                                    startActivity(i);
                                    if(getActivity() == null) return;
                                    getActivity().finish();
                                }
                            }

                            @Override
                            public void onError(ArrayList args) {
                                prgrs.dismiss();
                            }
                        });
                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.create().show();
            }
        });
        Abinding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
                SocketClient.closeSocket();
                Intent i = new Intent(getContext(), MainActivity.class);
                startActivity(i);
                if(getActivity() == null) return;
                getActivity().finish();
            }
        });
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
    public void ShowOrHideSave_Manager(String text, String original, int id){
        if(text.equals(original)){
            if(changed.contains(id+"")){
                changed.remove(id+"");
            }
        }
        else{
            changed.add(id+"");
        }
        ShowOrHideSaveBtn();
    }
    public void ShowOrHideSaveBtn(){
        if(binding == null)return;
        if(changed.size()>=1){
            Abinding.buttonSave.setVisibility(View.VISIBLE);
        }
        else{
            Abinding.buttonSave.setVisibility(View.GONE);
        }
    }
}