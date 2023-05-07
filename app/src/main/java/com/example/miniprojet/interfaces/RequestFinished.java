package com.example.miniprojet.interfaces;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public interface RequestFinished {
    public void onFinish(ArrayList args);
    public void onError(ArrayList args);

}
