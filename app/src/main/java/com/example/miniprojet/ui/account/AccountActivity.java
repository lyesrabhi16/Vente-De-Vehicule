package com.example.miniprojet.ui.account;

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
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.MainActivity;
import com.example.miniprojet.R;
import com.example.miniprojet.Server;
import com.example.miniprojet.SocketClient;
import com.example.miniprojet.databinding.ActivityAccountBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Client;
import com.example.miniprojet.models.User;
import com.example.miniprojet.ui.annonce.AnnoncesActivity;
import com.example.miniprojet.ui.messages.chat.ChatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class AccountActivity extends AppCompatActivity {

    private ActivityAccountBinding ABind;
    private int accountID;
    private Client client;
    private ArrayList changed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        ABind = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(ABind.getRoot());

        ABind.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        accountID = getIntent().getIntExtra("accountID", -1);
        User user = User.getInstance(getApplicationContext());
        if(accountID != -1){
            ProgressDialog prgrs = new ProgressDialog(AccountActivity.this);
            prgrs.setMessage("Getting user info...");
            prgrs.show();

            Client.getClient(accountID, getApplicationContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    prgrs.dismiss();
                    client = (Client) args.get(0);
                    ABind.nom.setText(client.getNom());
                    ABind.prenom.setText(client.getPrenom());
                    ABind.age.setText(client.getAge()+"");
                    ABind.email.setText(client.getEmail());
                    ABind.numTel.setText(client.getNumTel());
                    if(client.getIdClient() == user.getID()){
                        ABind.userAvatar.setClickable(true);
                        ABind.nomTextLayout.setEnabled(true);
                        ABind.prenomTextLayout.setEnabled(true);
                        ABind.ageTextLayout.setEnabled(true);
                        ABind.emailTextLayout.setEnabled(true);
                        ABind.numTelTextLayout.setEnabled(true);

                        ABind.buttonChangePassword.setVisibility(View.VISIBLE);
                        ABind.buttonDeleteAccount.setVisibility(View.VISIBLE);
                        ABind.buttonLogout.setVisibility(View.VISIBLE);
                        ABind.buttonSave.setVisibility(View.GONE);
                        ABind.buttonAnnonces.setVisibility(View.GONE);
                        ABind.buttonSendMessage.setVisibility(View.GONE);

                        if(user.getImageName() != null){
                            Bitmap avatar = Server.getBitmap(getApplicationContext(), user.getImageName());
                            if (avatar != null){
                                ABind.userAvatar.setImageBitmap(avatar);
                            }
                        }
                        else{
                            Server.getImage("imageClient-[" + user.getID() + "].jpeg", getApplicationContext(), new RequestFinished() {
                                @Override
                                public void onFinish(ArrayList args) {
                                    Bitmap img = (Bitmap) args.get(0);
                                    user.setImageName(Server.saveBitmap(getApplicationContext(), "imageClient", img));
                                    ABind.userAvatar.setImageBitmap(img);
                                }

                                @Override
                                public void onError(ArrayList args) {

                                }
                            });
                        }

                        changed = new ArrayList();
                        ABind.nom.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                ShowOrHideSave_Manager( String.join("", charSequence), user.getNom(),ABind.nom.getId());
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                ShowOrHideSave_Manager(editable.toString(), user.getNom(),ABind.nom.getId());

                            }
                        });
                        ABind.prenom.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                ShowOrHideSave_Manager(String.join("", charSequence), user.getPrenom(),ABind.prenom.getId());

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                ShowOrHideSave_Manager(editable.toString(), user.getPrenom(),ABind.prenom.getId());

                            }
                        });
                        ABind.age.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                ShowOrHideSave_Manager(String.join("", charSequence), user.getAge()+"",ABind.age.getId());

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                ShowOrHideSave_Manager(editable.toString(), user.getAge()+"",ABind.age.getId());

                            }
                        });
                        ABind.email.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                ShowOrHideSave_Manager(String.join("", charSequence), user.getEmail(),ABind.email.getId());

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                ShowOrHideSave_Manager(editable.toString(), user.getEmail(),ABind.email.getId());

                            }
                        });
                        ABind.numTel.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                ShowOrHideSave_Manager(String.join("", charSequence), user.getNumtel(),ABind.numTel.getId());

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                ShowOrHideSave_Manager(editable.toString(), user.getNumtel(),ABind.numTel.getId());
                            }
                        });

                    }

                }

                @Override
                public void onError(ArrayList args) {
                    prgrs.dismiss();
                }
            });
            ABind.buttonSendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent chat = new Intent(getApplicationContext(), ChatActivity.class);
                    chat.putExtra("userID", client.getIdClient());
                    chat.putExtra("accountName", client.getNom()+" "+ client.getPrenom());
                    startActivity(chat);
                }
            });
            ABind.buttonAnnonces.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent annonces = new Intent(getApplicationContext(), AnnoncesActivity.class);
                    annonces.putExtra("accountID", client.getIdClient());
                    startActivity(annonces);
                }
            });

            ABind.buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String lastname = ABind.nom.getText().toString().trim();
                    String firstname = ABind.prenom.getText().toString().trim();
                    String age = ABind.age.getText().toString().trim();
                    String email = ABind.email.getText().toString().trim();
                    String phonenumber = ABind.numTel.getText().toString().trim();
                    Client client = new Client(
                            user.getID(),
                            Integer.parseInt(age),
                            email,
                            phonenumber,
                            lastname,
                            firstname
                    );
                    ProgressDialog prgs = new ProgressDialog(AccountActivity.this);
                    prgs.setMessage("Updating...");
                    prgs.show();
                    Client.updateClient(client, getApplicationContext(), new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            if(ABind == null) return;
                            prgs.dismiss();
                            user.logout();
                            user.userLogin(
                                    client.getIdClient(),
                                    client.getEmail(),
                                    client.getNumTel(),
                                    client.getNom(),
                                    client.getPrenom(),
                                    client.getAge(),
                                    false
                            );
                            ABind.buttonSave.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(ArrayList args) {
                            if(ABind == null) return;
                            prgs.dismiss();
                        }
                    });
                }
            });
            ABind.buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(AccountActivity.this);
                    alert.setMessage("Are you sure you want to delete your account ?");
                    alert.setTitle(R.string.delete_account);
                    alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            ProgressDialog prgrs = new ProgressDialog(AccountActivity.this);
                            prgrs.setMessage("Deleting Account...");
                            prgrs.show();
                            Client.delClient(user.getID(), getApplicationContext(), new RequestFinished() {
                                @Override
                                public void onFinish(ArrayList args) {
                                    prgrs.dismiss();
                                    if(((String)args.get(0)).equals("success")){
                                        logout();
                                        SocketClient.closeSocket();
                                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();
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

            ABind.buttonChangePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), "to be implemented later.", Toast.LENGTH_SHORT).show();
                }
            });
            ABind.buttonLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logout();
                    SocketClient.closeSocket();
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                }
            });

            ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri = data.getData();
                            try {
                                Bitmap img = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), selectedImageUri);
                                user.setImageName(Server.saveBitmap(getApplicationContext(), "imageClient", img));
                                JSONObject o = new JSONObject();
                                o.put("imgB64", Server.ImageToBase64(img, getApplicationContext()));
                                o.put("id", user.getID());
                                o.put("format", "jpeg");
                                o.put("type", Server.TYPE_IMAGE_AVATAR);
                                Server.sendImageToserver(o, getApplicationContext());
                                ABind.userAvatar.setImageURI(selectedImageUri);
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "Error : "+e, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Error : "+e, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }
            });

            ABind.userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(gallery);
                }
            });

        }

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
        if(ABind == null)return;
        if(changed.size()>=1){
            ABind.buttonSave.setVisibility(View.VISIBLE);
        }
        else{
            ABind.buttonSave.setVisibility(View.GONE);
        }
    }
    public void logout(){
        ProgressDialog prgrs = new ProgressDialog(getApplicationContext());
        prgrs.setMessage("Logout in progress...");
        User user = User.getInstance(getApplicationContext());
        user.logout();
        if(!user.isLoggedin())
            Toast.makeText(getApplicationContext(),"logout successful.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(),"logout failed.", Toast.LENGTH_LONG).show();
        prgrs.dismiss();

    }
}