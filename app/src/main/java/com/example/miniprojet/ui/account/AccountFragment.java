package com.example.miniprojet.ui.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.example.miniprojet.databinding.ActivityMainBinding;
import com.example.miniprojet.databinding.FragmentAccountBinding;
import com.example.miniprojet.databinding.FragmentAccountSignInBinding;
import com.example.miniprojet.databinding.FragmentAuthBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.User;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class AccountFragment extends Fragment {

    private FragmentAccountSignInBinding SignInbinding;
    private FragmentAccountBinding AccountBinding;
    private ActivityMainBinding MainBinding;
    private FragmentAuthBinding AuthBinding;
    private Server dbc;
    private ArrayList changed;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root;
        User user = User.getInstance(getContext());
        if ( user.isLoggedin() ){
            AccountBinding = FragmentAccountBinding.inflate(inflater, container, false);
            root = AccountBinding.getRoot();

            AccountBinding.nom.setText(user.getNom());
            AccountBinding.prenom.setText(user.getPrenom());
            AccountBinding.age.setText(user.getAge()+"");
            AccountBinding.email.setText(user.getEmail());
            AccountBinding.numTel.setText(user.getNumtel());

            if(user.getImageName() != null){
                Bitmap avatar = Server.getBitmap(getContext(), user.getImageName());
                if (avatar != null){
                    AccountBinding.userAvatar.setImageBitmap(avatar);
                }
            }
            else{
                Server.getImage("imageClient-[" + user.getID() + "].jpeg", getContext(), new RequestFinished() {
                    @Override
                    public void onFinish(ArrayList args) {
                        Bitmap img = (Bitmap) args.get(0);
                        user.setImageName(Server.saveBitmap(getContext(), "imageClient", img));
                        AccountBinding.userAvatar.setImageBitmap(img);
                    }

                    @Override
                    public void onError(ArrayList args) {

                    }
                });
            }

            changed = new ArrayList();
            AccountBinding.nom.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        ShowOrHideSave_Manager( String.join("", charSequence), user.getNom(),AccountBinding.nom.getId());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), user.getNom(),AccountBinding.nom.getId());

                }
            });
            AccountBinding.prenom.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), user.getPrenom(),AccountBinding.prenom.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), user.getPrenom(),AccountBinding.prenom.getId());

                }
            });
            AccountBinding.age.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), user.getAge()+"",AccountBinding.age.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), user.getAge()+"",AccountBinding.age.getId());

                }
            });
            AccountBinding.email.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), user.getEmail(),AccountBinding.email.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), user.getEmail(),AccountBinding.email.getId());

                }
            });
            AccountBinding.numTel.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), user.getNumtel(),AccountBinding.numTel.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), user.getNumtel(),AccountBinding.numTel.getId());
                }
            });


            AccountBinding.buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), "to be implemented later.", Toast.LENGTH_SHORT).show();
                }
            });

            AccountBinding.buttonChangePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), "to be implemented later.", Toast.LENGTH_SHORT).show();
                }
            });
            AccountBinding.buttonLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        logout();
                        SocketClient.closeSocket();
                        Intent i = new Intent(getContext(), MainActivity.class);
                        startActivity(i);
                        getActivity().finish();
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
                                Bitmap img = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                                user.setImageName(Server.saveBitmap(getContext(), "imageClient", img));
                                JSONObject o = new JSONObject();
                                o.put("imgB64", Server.ImageToBase64(img, getContext()));
                                o.put("id", user.getID());
                                o.put("format", "jpeg");
                                o.put("type", Server.TYPE_IMAGE_AVATAR);
                                Server.sendImageToserver(o, getContext());
                                AccountBinding.userAvatar.setImageURI(selectedImageUri);
                            } catch (IOException e) {
                                Toast.makeText(getContext(), "Error : "+e, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Toast.makeText(getContext(), "Error : "+e, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }
            });

            AccountBinding.userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(gallery);
                }
            });

        }
        else {
            AuthBinding = FragmentAuthBinding.inflate(inflater, container, false);
            root = AuthBinding.getRoot();




        }

        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (!User.getInstance(getContext()).isLoggedin()) {
            super.onViewCreated(view, savedInstanceState);
            AuthBinding.navControllerAuth.setSelectedItemId(R.id.accountSignInFragment);

            AccountSignInFragment SignInFragment = new AccountSignInFragment();
            AccountRegisterFragment RegFragment = new AccountRegisterFragment();

            getActivity().getSupportFragmentManager().beginTransaction().replace(AuthBinding.navHostFragmentAuth.getId(), SignInFragment).commitNow();

            AuthBinding.navControllerAuth.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.accountRegisterFragment:
                            getActivity().getSupportFragmentManager().beginTransaction().replace(AuthBinding.navHostFragmentAuth.getId(), RegFragment).commitNow();
                            return true;
                        case R.id.accountSignInFragment:
                            getActivity().getSupportFragmentManager().beginTransaction().replace(AuthBinding.navHostFragmentAuth.getId(), SignInFragment).commitNow();
                            return true;
                    }
                    return false;
                }
            });
        }
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
        if(AccountBinding == null)return;
        if(changed.size()>=1){
            AccountBinding.buttonSave.setVisibility(View.VISIBLE);
        }
        else{
            AccountBinding.buttonSave.setVisibility(View.GONE);
        }
    }

}
