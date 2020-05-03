package com.pixelw.net;

import com.pixelw.entity.Client;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Carl Su
 * @date 2020/4/30
 */
public abstract class ServerCore {

    public static final String CONTROL_TOKEN = "JBdKZ7g7sub8bP3";
    protected ExecutorService threadPool;
    protected ServerListener listener;
    protected int port;

    public ServerCore(ServerListener listener, int port) {
        this.listener = listener;
        this.port = port;
        threadPool = Executors.newCachedThreadPool();
    }

    public abstract void run();

    public abstract void sendTextMsg(String msg, Client client);

    public abstract void shutdown(Map<String, Client> map);
}
