package com.pixelw.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pixelw.beans.IMMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketCore {

    private int serverPort = 9832;
    private static final String CLOSE_TOKEN = "JBdKZ7g7sub8bP3";
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private Gson gson;

    Map<String, Socket> socketMap = new HashMap<>();


    public SocketCore() {
        try {
            threadPool = Executors.newCachedThreadPool();
            InetAddress mHost = InetAddress.getLocalHost();
             gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd H:mm:ss")
                    .create();
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
                System.out.println("Port: " + serverPort);
                //阻塞 直到有socket连接
                Socket socket = serverSocket.accept();
                //存入<IP,socket> map
                socketMap.put(socket.getInetAddress().toString(), socket);
                //递归 新线程等待下一个客户端
                waitNewClient();
                System.out.println(socket.getInetAddress() + " connected");
                listenClient(socket);
            } catch (IOException e) {
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
                //todo 不能发送多行
                receivedMsg = bufferedReader.readLine();
            } while (receiveTextMsg(receivedMsg, socket.getInetAddress().toString()));
            socket.close();
            System.out.println(socket.getInetAddress().toString() + " disconnected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        //foreach 或者Iterator
        try {
            if (socketMap.size() > 0){
                for (String strAddress : socketMap.keySet()) {
                    Socket socket = socketMap.get(strAddress);
                    sendViaSocket(socket,CLOSE_TOKEN);
                    socket.close();
                }
            }else{
                System.out.println("no opened sockets");
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error on stop");
        }

    }

    private boolean receiveTextMsg(String msg, String source) {
        if (msg.equals(CLOSE_TOKEN)) {
            return false;
        } else {
            IMMessage imMessage = gson.fromJson(msg, IMMessage.class);
            imMessage.setMsgSource(source);
            System.out.println(imMessage.getMsgUser() + ": " + imMessage.getMsgBody());
            return true;
        }

    }


    public void sendTextMsg(String msg, String ipAddress) {
        Socket socket;
        if (socketMap.containsKey(ipAddress)) {
            socket = socketMap.get(ipAddress);
            sendViaSocket(socket, msg);
        } else {
            System.out.println("Error: client " + ipAddress + " not found.");
        }

    }

    private void sendViaSocket(Socket socket, String msg) {
        try {
            if (!socket.isClosed()) {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write((msg + "\n").getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } else {
                System.out.println("Error: socket not ready");
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
