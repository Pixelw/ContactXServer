package com.pixelw;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pixelw.entity.Client;

import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientsHandler {

    //todo 12/17 生成分发客户端ID

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

    private void newClient(String userID, Client client) {
        stringClientMap.put(userID, client);
    }

    public boolean removeClient(Client client) {
        String userId = client.getUserId();
        if (userId != null) {
            return stringClientMap.remove(userId) == client;
        }
        return false;
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
        if (message.startsWith("Iam:")) {
            String userId = message.substring(4);
            //System.out.println(subStr + " from " + socket.getInetAddress());
            //存入<userID,socket> map

            //todo mark new clinet online
            client.setUserId(userId);
            newClient(userId, client);
        } else {
            JsonObject jsonObject;
            try {
                jsonObject = JsonParser.parseString(message).getAsJsonObject();
                if (jsonObject.has("chatMsg")) {
                    forwardToTarget(jsonObject.getAsJsonObject("chatMsg"), message);
                } else if (jsonObject.has("usersCrc32")) {
                    returnOnlineUsers(jsonObject.getAsJsonArray("usersCrc32"), client);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void returnOnlineUsers(JsonElement usersCrc32, Client client) {
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> crc32s = gson.fromJson(usersCrc32, type);
        List<String> onlineUsersCrc32 = new ArrayList<>();
        stringClientMap.put("2662605079", new Client(new Socket()));
        for (String crc32 : crc32s) {
            if (stringClientMap.getOrDefault(crc32, null) != null) {
                onlineUsersCrc32.add(crc32);
            }
        }
        callback.send(client, jsonMsgWrapper("onlineUsersCrc32", gson.toJsonTree(onlineUsersCrc32)));
    }

    private void forwardToTarget(JsonObject jsonObject, String originalMsg) {
//        IMMessage imMessage = null;
//        try {
//            imMessage = gson.fromJson(element, IMMessage.class);
//            System.out.println(imMessage.getMsgUser() + ": " + imMessage.getMsgBody());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (imMessage != null && imMessage.getMsgDestination() != null) {
//            String targetUser = imMessage.getMsgDestination();
//            Client client = findUserClient(targetUser);
//            if (client != null) {
//                callback.send(client, message);
//            }
//        }
        String destId = jsonObject.get("msgDestination").getAsString();
        if (destId == null) {
            System.out.println("forwardToTarget: dest user not specified");
        } else {
            Client client = findUserClient(destId);
            if (client == null) {
                System.out.println("forwardToTarget: dest user " + destId + " is not online");
            } else {
                System.out.println("forwardToTarget: a message to " + destId);
                callback.send(client, originalMsg);
            }
        }
    }

    public String jsonMsgWrapper(String msgType, JsonElement element) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(msgType, element);
        return jsonObject.toString();
    }

    public interface Callback {
        void send(Client client, String string);
    }

}
