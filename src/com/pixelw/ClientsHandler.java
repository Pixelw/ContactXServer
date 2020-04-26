package com.pixelw;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pixelw.beans.IMMessage;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientsHandler {

    //todo 12/17 生成分发客户端ID

    private Map<String, Socket> userID_Socket_map = new HashMap<>();

    private ClientsHandler.Callback callback;

    private Gson gson;

    public Map<String, Socket> getUserID_Socket_map() {
        return userID_Socket_map;
    }


    public ClientsHandler() {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd H:mm:ss")
                .create();

    }


    public void newClient(String userID, Socket socket) {
        userID_Socket_map.put(userID, socket);
    }

    public void setCallback(Callback callback){
        this.callback = callback;
    }

    public Socket findUserSocket(String targetUser) {
        if (userID_Socket_map.containsKey(targetUser)) {
            return userID_Socket_map.get(targetUser);
        }
        return null;
    }

    public void handleClientMessage(String message, Socket socket) {
        if (message.startsWith("Iam:")) {
            String subStr = message.substring(4);
            System.out.println(subStr + " from " + socket.getInetAddress());
            //存入<userID,socket> map
            newClient(subStr, socket);
        } else {
            forwardToTarget(message);
        }
    }

    private void forwardToTarget(String message) {
        IMMessage imMessage = null;
        try {
            imMessage = gson.fromJson(message, IMMessage.class);
            System.out.println(imMessage.getMsgUser() + ": " + imMessage.getMsgBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (imMessage != null && imMessage.getMsgDestination() != null) {
            String targetUser = imMessage.getMsgDestination();
            Socket socketTargetUser = findUserSocket(targetUser);
            if (socketTargetUser != null) {
                callback.send(socketTargetUser,message);
            }
        }
    }

    public interface Callback {
        void send(Socket socket, String string);
    }

}
