package com.pixelw;

import com.pixelw.net.SocketCore;
import com.pixelw.net.SocketListener;

import java.net.Socket;

/**
 * @author Carl Su
 * @date 2020/4/26
 */
public class IMController implements SocketListener {

    private ClientsHandler clientsHandler;
    private final SocketCore socketCore;

    public IMController() {
        socketCore = new SocketCore(this,9832);
        socketCore.bindPort();
        clientsHandler = new ClientsHandler();
        clientsHandler.setCallback(new ClientsHandler.Callback() {
            @Override
            public void send(Socket socket, String string) {
                sendViaSocket(socket, string);
            }
        });
    }

    public void sendViaSocket(Socket socket, String msg){
        socketCore.sendViaSocket(socket, msg);
    }

    @Override
    public void onOpen(SocketCore socketCore, Socket socket) {
        System.out.println(socket.getInetAddress() + " connected");
    }

    @Override
    public void onMessage(SocketCore socketCore, Socket socket, String message) {
        clientsHandler.handleClientMessage(message, socket);
    }

    @Override
    public void onDisconnecting(SocketCore socketCore, Socket socket) {
        System.out.println(socket.getInetAddress().toString() + " intended to disconnect");
    }

    @Override
    public void onDisconnected(SocketCore socketCore, Socket socket) {
        System.out.println(socket.getInetAddress().toString() + " disconnected");
    }

    @Override
    public void onClosed(SocketCore core) {
        System.out.println("server closed");
    }


    public void finish() {
        socketCore.closeConnection(clientsHandler.getUserID_Socket_map());
    }

    public void sendTextMsg(String userId, String msg) {
        Socket socket = clientsHandler.findUserSocket(userId);
        socketCore.sendViaSocket(socket,msg);

    }
}
