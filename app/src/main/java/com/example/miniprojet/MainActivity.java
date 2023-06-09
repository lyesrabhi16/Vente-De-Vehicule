package com.example.miniprojet;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.miniprojet.databinding.ActivityMainBinding;
import com.example.miniprojet.models.User;
import com.example.miniprojet.ui.search.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static boolean keepConnected = false;
    private int nav;
    private NavController navController;
    private BottomNavigationView navView;
    private Socket client;

    public static boolean KeepConnected() {
        return keepConnected;
    }

    public static void setKeepConnected(boolean keepConnected) {
        MainActivity.keepConnected = keepConnected;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        updateBtnAccount(binding, 0);
        User user = User.getInstance(getApplicationContext());
        navView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        nav = R.id.navigation_home;
        if (user.isLoggedin()){

            client = SocketClient.connectSocket();
            client.on("request-init", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SocketClient.getSocket().emit("init", user.getID());
                        }
                    });
                }
            });
            if(user.isAdmin()){
                binding.buttonAccount.setClickable(false);
                binding.navView.setVisibility(View.INVISIBLE);
                binding.navViewAdmin.setVisibility(View.VISIBLE);
                findViewById(R.id.nav_host_fragment_activity_main).setVisibility(View.GONE);
                findViewById(R.id.nav_host_fragment_activity_main_admin).setVisibility(View.VISIBLE);
                navView = binding.navViewAdmin;
                navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main_admin);
                nav = R.id.annoncesFragment;
            }
            else{
                binding.navView.setVisibility(View.VISIBLE);
                binding.navViewAdmin.setVisibility(View.GONE);
                findViewById(R.id.nav_host_fragment_activity_main).setVisibility(View.VISIBLE);
                findViewById(R.id.nav_host_fragment_activity_main_admin).setVisibility(View.GONE);
            }

        }
        else{
            binding.navView.setVisibility(View.VISIBLE);
            binding.navViewAdmin.setVisibility(View.GONE);
            findViewById(R.id.nav_host_fragment_activity_main).setVisibility(View.VISIBLE);
            findViewById(R.id.nav_host_fragment_activity_main_admin).setVisibility(View.GONE);
        }





        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_messages, R.id.button_account, R.id.searchFragment)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        */
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    //admin
                    case R.id.annoncesFragment:
                        nav = R.id.annoncesFragment;
                        navController.navigate(R.id.annoncesFragment);
                        return true;
                    case R.id.messagesFragment:
                        nav = R.id.messagesFragment;
                        navController.navigate(R.id.messagesFragment);
                        return true;
                    case R.id.rendezVousFragment:
                        nav = R.id.rendezVousFragment;
                        navController.navigate(R.id.rendezVousFragment);
                        return true;
                    case R.id.reservationsFragment:
                        nav = R.id.reservationsFragment;
                        navController.navigate(R.id.reservationsFragment);
                        return true;
                    case R.id.adminPanelFragment:
                        nav = R.id.adminPanelFragment;
                        navController.navigate(R.id.adminPanelFragment);
                        return true;
                    //client
                    case R.id.navigation_home:
                        nav = R.id.navigation_home;
                        navController.navigate(R.id.navigation_home);
                        return true;
                    case R.id.navigation_dashboard:
                        nav = R.id.navigation_dashboard;
                        navController.navigate(R.id.navigation_dashboard);
                        return true;
                    case R.id.navigation_messages:
                        nav = R.id.navigation_messages;
                        navController.navigate(R.id.navigation_messages);
                        return true;
                    case R.id.navigation_notifications:
                        nav = R.id.navigation_notifications;
                        navController.navigate(R.id.navigation_notifications);
                        return true;
                    case R.id.accountFragment:
                        switch (nav) {
                            case R.id.searchFragment:
                                navController.navigate(R.id.searchFragment);
                                return true;
                            case R.id.accountFragment:
                                navController.navigate(R.id.accountFragment);
                                return true;
                        }
                        return true;
                }
                return false;
            }
        });

        binding.buttonAccount.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(nav != R.id.accountFragment){
                            nav = R.id.accountFragment;
                            navView.setSelectedItemId(R.id.accountFragment);
                        }
                    }

                }
        );
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()){
                    navView.setSelectedItemId(R.id.navigation_home);
                    return false;
                }
                SearchFragment.setSearchTerm(binding.searchView.getQuery().toString());
                nav = R.id.searchFragment;
                navView.setSelectedItemId(R.id.accountFragment);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return onQueryTextSubmit(newText);
            }
        });
        binding.searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchFragment.setSearchTerm(binding.searchView.getQuery().toString());
                nav = R.id.searchFragment;
                navView.setSelectedItemId(R.id.accountFragment);
            }
        });

    }

    public void updateBtnAccount(ActivityMainBinding binding,int counter){
        User user = User.getInstance(getApplicationContext());
        Button btn_account = binding.buttonAccount;

        if(user.isLoggedin())
            btn_account.setText(user.getNom());
        else{
            btn_account.setText(R.string.title_account);
            counter++;
            if(counter<=3){
                updateBtnAccount(binding, counter);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (keepConnected){
            setKeepConnected(false);
            return;
        }
        SocketClient.closeSocket();
    }
}