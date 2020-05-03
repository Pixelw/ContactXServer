package com.pixelw;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pixelw.entity.Client;
import com.pixelw.entity.IMMessage;

import java.util.HashMap;
import java.util.Map;

public class ClientsHandler {

    //todo 12/17 生成分发客户端ID

    private static final String CONTROL_TOKEN = "JBdKZ7g7sub8bP3";
    private Map<String, Client> stringClientMap = new HashMap<>();

    private ClientsHandler.Callback callback;

    private Gson gson;

    public Map<String, Client> getStringClientMap() {
        return stringClientMap;
    }


    public ClientsHandler() {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd H:mm:ss")
                .create();

    }

    public void newClient(String userID, Client client) {
        stringClientMap.put(userID, client);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public Client findUserClient(String targetUser) {
        if (stringClientMap.containsKey(targetUser)) {
            return stringClientMap.get(targetUser);
        }
        return null;
    }

    public void handleClientMessage(String message, Client client) {
        if (message.startsWith(CONTROL_TOKEN + "Iam:")) {
            String subStr = message.substring(CONTROL_TOKEN.length() + 4);
//            System.out.println(subStr + " from " + socket.getInetAddress());
            //存入<userID,socket> map
            newClient(subStr, client);
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
            Client client = findUserClient(targetUser);
            if (client != null) {
                callback.send(client, message);
            }
        }
    }

    public interface Callback {
        void send(Client client, String string);
    }

}
