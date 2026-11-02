package com.example.miniprojet;

import com.example.miniprojet.models.User;

import java.util.Collections;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketClient {
    private static Socket socket;

    public static Socket getSocket() {
        return socket;
    }

    public static void setSocket(Socket socket) {
        SocketClient.socket = socket;
    }

    public static Socket connectSocket(){
        try {
            String token = User.getInstance(MiniProjetApp.context()).getToken();
            if (token == null) {
                return null; // le serveur n'accepte que les connexions authentifiées
            }
            IO.Options options = new IO.Options();
            options.auth = Collections.singletonMap("token", token);
            socket = IO.socket(Server.getUrlServer(), options);
            socket.connect();
            socket.emit("client-message", "hello from client");

            return socket;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeSocket(){
        if(socket == null) return;
        if(socket.connected())
        {
            socket.off();
            socket.disconnect();
            socket = null;
        }
    }
}
