package com.pixelw.net;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketCore {

    private int serverPort = 9832;
    private static final String CLOSE_TOKEN = "JBdKZ7g7sub8bP3";
    private ServerSocket serverSocket;

    public SocketCore() {
        try {
            InetAddress mHost = InetAddress.getLocalHost();
            String hostname = mHost.getHostName();
            String hostIP = mHost.getHostAddress();
            System.out.println("ContactXServer init.\nPowered by Pixelw.");
            System.out.println("Host: " + hostname);
            System.out.println("IP: " + hostIP);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Init failed..");
        }
    }


    public void bindPort() {
        try {
            serverSocket = new ServerSocket(serverPort);
            waitNewClient();
        } catch (IOException e) {
            System.out.println("error binding port" + serverPort);
            e.printStackTrace();
        }
    }

    //开启一个新线程 从serversocket.accept -> socket -> readline -> 关闭

    

    public void waitNewClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Port: " + serverPort);
                    //阻塞 直到有socket连接
                    Socket socket = serverSocket.accept();
                    //递归 新线程等待下一个客户端
                    waitNewClient();
                    System.out.println(socket.getInetAddress() + " connected");
                    listenClient(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
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
                //todo 不能发送多行
                receivedMsg = bufferedReader.readLine();
            } while (receiveTextMsg(receivedMsg, socket.getInetAddress().toString()));
            socket.close();
            System.out.println(socket.getInetAddress().toString() + " disconnected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO 关闭所有连接
    public void closeConnection() {

    }

    private boolean receiveTextMsg(String msg, String source) {
        if (msg.equals(CLOSE_TOKEN)) {
            return false;
        } else {
            System.out.println(source + " says: " + msg);
            return true;
        }

    }


    //todo 给选的客户端发消息
    public void sendViaSocket(String msg, Socket socket) {
        try {
            if (socket.isConnected()) {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } else {
                System.out.println("no client yet.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
