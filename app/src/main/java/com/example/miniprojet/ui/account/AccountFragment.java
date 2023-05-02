package com.example.miniprojet.ui.account;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.material.navigation.NavigationBarView;

public class AccountFragment extends Fragment {

    private FragmentAccountSignInBinding SignInbinding;
    private FragmentAccountBinding AccountBinding;
    private ActivityMainBinding MainBinding;
    private FragmentAuthBinding AuthBinding;
    private Server dbc;
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

}
