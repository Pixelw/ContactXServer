package com.pixelw.net;

import com.pixelw.entity.Client;

/**
 * @author Carl Su
 * @date 2020/4/26
 */
public interface ServerListener {

    void onOpen(ServerCore core, Client client);

    void onMessage(ServerCore core, Client client, String message);

//    public abstract void onMessage(Socket socket, Byte[] bytes);

    void onDisconnecting(ServerCore core, Client client);

    void onDisconnected(ServerCore core, Client client);

    void onClosed(ServerCore core);

}
