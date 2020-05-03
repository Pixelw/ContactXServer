package com.pixelw.net.legacy;

import com.pixelw.entity.Client;
import com.pixelw.net.ServerListener;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SocketCore extends com.pixelw.net.ServerCore {

    private ServerSocket serverSocket;


    public SocketCore(ServerListener serverListener, int serverPort) {
        super(serverListener, serverPort);
        try {
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

    @Override
    public void sendTextMsg(String msg, Client client) {
        Socket socketTargetUser = client.getSocket();
        if (socketTargetUser != null) {
            sendViaSocket(socketTargetUser, msg);
        }
    }

    @Override
    public void shutdown(Map<String, Client> map) {
        //foreach 或者Iterator
        try {
            if (map.size() > 0) {
                for (String strUserID : map.keySet()) {
                    Client client = map.get(strUserID);
                    Socket socket = client.getSocket();
                    sendViaSocket(socket, CONTROL_TOKEN);
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
            listener.onClosed(this);
        }

    }


    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            waitNewClient();
        } catch (IOException e) {
            System.out.println("Error: error binding port" + port);
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
                    Client client = new Client(socket);
                    listener.onOpen(this, client);
                    listenClient(client);
                }
            } catch (IOException e) {
                System.out.println("error on wait");
                e.printStackTrace();
            }
        });
    }

    private void listenClient(Client client) {
        String receivedMsg;
        try {
            InputStream inputStream;
            do {
                inputStream = client.getSocket().getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                //阻塞 直到客户端发送"\n"
                receivedMsg = bufferedReader.readLine();
            } while (receive(receivedMsg, client));
            client.getSocket().close();
            listener.onDisconnected(this, client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理接收消息
     * 当消息为CLOSE_TOKEN的内容时，返回false结束loop
     *
     * @param msg    收到的消息
     * @param client 对应的连接客户端
     * @return 是否保持连接
     */
    private boolean receive(String msg, Client client) {

        if (msg != null && msg.equals(CONTROL_TOKEN)) {
            listener.onDisconnecting(this, client);
            return false;
        }
        listener.onMessage(this, client, msg);
//        clientsHandler.handleClientMessage(msg, socket);
        return true;

    }

    private void sendViaSocket(Socket socket, String msg) {
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
