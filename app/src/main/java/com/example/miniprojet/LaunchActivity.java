package com.example.miniprojet;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

import com.example.miniprojet.databinding.ActivityLaunchBinding;


public class LaunchActivity extends AppCompatActivity {

    private ActivityLaunchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLaunchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //hide support action bar
        getSupportActionBar().hide();
        //start main activity
        final Intent main = new Intent(LaunchActivity.this, MainActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(main);
                finish();
            }
        },1000);

    }

}