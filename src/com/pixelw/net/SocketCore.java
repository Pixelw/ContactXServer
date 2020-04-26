package com.pixelw.net;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketCore {

    private int serverPort;
    private static final String CLOSE_TOKEN = "JBdKZ7g7sub8bP3";
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private SocketListener socketListener;


    public SocketCore(SocketListener socketListener, int serverPort) {
        this.socketListener = socketListener;
        this.serverPort = serverPort;
        try {
            threadPool = Executors.newCachedThreadPool();
            InetAddress mHost = InetAddress.getLocalHost();
            String hostname = mHost.getHostName();
            String hostIP = mHost.getHostAddress();
            System.out.println("ContactXServer init.\nPowered by Pixelw.");
            System.out.println("Host: " + hostname);
            System.out.println("IP: " + hostIP);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: Init failed..");
        }
    }


    public void bindPort() {
        try {
            serverSocket = new ServerSocket(serverPort);
            waitNewClient();
        } catch (IOException e) {
            System.out.println("Error: error binding port" + serverPort);
            e.printStackTrace();
        }
    }

    //开启一个新线程 从serversocket.accept -> socket -> readline -> 关闭

    public void waitNewClient() {
        threadPool.execute(() -> {
            try {
                //阻塞 直到有socket连接
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    //递归 新线程等待下一个客户端
                    waitNewClient();
                    socketListener.onOpen(this, socket);
                    listenClient(socket);
                }
            } catch (IOException e) {
                System.out.println("error on wait");
                e.printStackTrace();
            }
        });
    }

    private void listenClient(Socket socket) {
        String receivedMsg;
        try {
            InputStream inputStream;
            do {
                inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                //阻塞 直到客户端发送"\n"
                receivedMsg = bufferedReader.readLine();
            } while (receive(receivedMsg, socket));
            socket.close();
            socketListener.onDisconnected(this, socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理接收消息
     * 当消息为CLOSE_TOKEN的内容时，返回false结束loop
     *
     * @param msg    收到的消息
     * @param socket 对应的连接socket
     * @return 是否保持连接
     */
    private boolean receive(String msg, Socket socket) {
        if (msg.equals(CLOSE_TOKEN)) {
            socketListener.onDisconnecting(this, socket);
            return false;
        }
        socketListener.onMessage(this, socket, msg);
//        clientsHandler.handleClientMessage(msg, socket);
        return true;

    }

    public void closeConnection(Map<String, Socket> map) {
        //foreach 或者Iterator
        try {
            if (map.size() > 0) {
                for (String strUserID : map.keySet()) {
                    Socket socket = map.get(strUserID);
                    sendViaSocket(socket, CLOSE_TOKEN);
                    socket.close();
                }
            } else {
                System.out.println("no opened sockets");
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error on stop");
        } finally {
            threadPool.shutdown();
        }

    }


    public void sendTextMsg(String msg, Socket socketTargetUser) {
        if (socketTargetUser != null) {
            sendViaSocket(socketTargetUser, msg);
        }
    }

    public void sendViaSocket(Socket socket, String msg) {
        try {
            if (!socket.isClosed()) {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write((msg + "\n").getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } else {
                System.out.println("Error: client" + socket.getInetAddress() + "disconnected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
