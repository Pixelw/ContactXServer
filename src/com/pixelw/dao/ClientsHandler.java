package com.pixelw.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pixelw.beans.IMMessage;
import com.pixelw.net.SocketCore;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientsHandler {


    private Map<String, Socket> socket_IP_Map = new HashMap<>();
    private Map<String, String> user_IP_Map = new HashMap<>();
    private Gson gson;
    private SocketCore core;

    public ClientsHandler(SocketCore core) {
        this.core = core;
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd H:mm:ss")
                .create();

    }

    public Map<String, Socket> getSocket_IP_Map() {
        return socket_IP_Map;
    }


    public void newClient(String ipAddress, Socket socket){
        socket_IP_Map.put(ipAddress, socket);
    }

    public void handleClientMessage(String message ,String source){
        if (message.startsWith("Iam:")) {
            String subStr = message.substring(4);
            System.out.println(subStr+" from "+source);
            user_IP_Map.put(subStr, source);
        } else {
            IMMessage imMessage = gson.fromJson(message, IMMessage.class);
            if (imMessage.getMsgDestination() != null) {
                String targetUser = imMessage.getMsgDestination();
                core.sendTextMsg(message, findUserAddress(targetUser));
            }
            System.out.println(imMessage.getMsgUser() + ": " + imMessage.getMsgBody());
        }
    }

    private String findUserAddress(String targetUser) {
        if (user_IP_Map.containsKey(targetUser)) {
            return user_IP_Map.get(targetUser);
        }
        return null;
    }
}
