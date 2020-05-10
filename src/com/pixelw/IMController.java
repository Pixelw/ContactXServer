package com.pixelw;

import com.pixelw.entity.Client;
import com.pixelw.net.ServerCore;
import com.pixelw.net.ServerListener;
import com.pixelw.net.legacy.SocketCore;

/**
 * @author Carl Su
 * @date 2020/4/26
 */
public class IMController implements ServerListener {

    private ClientsHandler clientsHandler;
    public static final int SERVER_PORT = 9832;
    private final ServerCore serverCore;

    public IMController() {
        clientsHandler = new ClientsHandler();
        clientsHandler.setCallback(new ClientsHandler.Callback() {
            @Override
            public void send(Client client, String string) {
                IMController.this.send(client, string);
            }
        });

        serverCore = new SocketCore(this, SERVER_PORT);
        serverCore.run();
    }

    public void send(Client client, String msg) {
        serverCore.sendTextMsg(msg, client);
    }

    @Override
    public void onOpen(ServerCore serverCore, Client client) {
        System.out.println(client.getInetAddress());
    }

    @Override
    public void onMessage(ServerCore socketCore, Client client, String message) {
        System.out.println("receive:" + message);
        clientsHandler.handleClientMessage(message, client);
    }

    @Override
    public void onDisconnecting(ServerCore socketCore, Client client) {
        System.out.println(client.getInetAddress() + " intended to disconnect");
    }

    @Override
    public void onDisconnected(ServerCore socketCore, Client client) {
        System.out.println(client.getInetAddress() + " disconnected " + clientsHandler.removeClient(client));
    }

    @Override
    public void onClosed(ServerCore core) {
        System.out.println("server closed");
    }


    public void finish() {
        serverCore.shutdown(clientsHandler.getStringClientMap());
    }

    public void sendTextMsg(String userId, String msg) {
        Client client = clientsHandler.findUserClient(userId);
        System.out.println("send" + msg);
        serverCore.sendTextMsg(msg, client);
    }
}
