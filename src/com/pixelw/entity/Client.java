package com.pixelw.entity;

import io.netty.channel.Channel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Carl Su
 * @date 2020/4/30
 */
public class Client {
    public static final int TYPE_LEGACY = 0;
    public static final int TYPE_NETTY = 1;
    private Socket socket;
    private Channel channel;
    private int type;

    public Client(Socket socket) {
        this.type = TYPE_LEGACY;
        this.socket = socket;
    }

    public Client(Channel channel) {
        this.type = TYPE_NETTY;
        this.channel = channel;
    }

    public Socket getSocket() {
        return socket;
    }

    public Channel getChannel() {
        return channel;
    }

    public InetAddress getInetAddress() {
        switch (type) {
            case TYPE_LEGACY:
                return socket.getInetAddress();
            case TYPE_NETTY:
                return ((InetSocketAddress) channel.remoteAddress()).getAddress();
            default:
                return null;
        }
    }

    public Object getConnection() {
        switch (type) {
            case TYPE_LEGACY:
                return socket;
            case TYPE_NETTY:
                return channel;
            default:
                return null;
        }
    }
}
