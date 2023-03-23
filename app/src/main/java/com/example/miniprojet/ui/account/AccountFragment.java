package com.example.miniprojet.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.miniprojet.databinding.FragmentAccountSignInBinding;
import com.example.miniprojet.databinding.FragmentDashboardBinding;

public class AccountFragment extends Fragment {

    private FragmentAccountSignInBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentAccountSignInBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }
}
