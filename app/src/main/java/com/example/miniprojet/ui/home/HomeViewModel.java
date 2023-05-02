package com.example.miniprojet.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.miniprojet.Server;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private Server DBC;

    public HomeViewModel() {
        DBC = new Server();
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}