package com.example.miniprojet;

import android.app.Application;
import android.content.Context;

/** Donne à AuthRequest et à SocketClient un contexte, sans qu'ils le reçoivent à chaque appel. */
public class MiniProjetApp extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context context() {
        return context;
    }
}
