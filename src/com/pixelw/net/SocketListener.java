package com.pixelw.net;

import java.net.Socket;

/**
 * @author Carl Su
 * @date 2020/4/26
 */
public interface SocketListener {

    void onOpen(SocketCore core, Socket socket);

    void onMessage(SocketCore core, Socket socket, String message);

//    public abstract void onMessage(Socket socket, Byte[] bytes);

    void onDisconnecting(SocketCore core, Socket socket);

    void onDisconnected(SocketCore core, Socket socket);

    void onClosed(SocketCore core);

}
