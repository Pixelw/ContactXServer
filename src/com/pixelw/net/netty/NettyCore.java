package com.pixelw.net.netty;

import com.pixelw.entity.Client;
import com.pixelw.net.ServerCore;
import com.pixelw.net.ServerListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.util.Map;

/**
 * @author Carl Su
 * @date 2020/5/1
 */
public class NettyCore extends ServerCore {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyCore(ServerListener listener, int port) {
        super(listener, port);
    }

    @Override
    public void run() {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                bossGroup = new NioEventLoopGroup();
                workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel socketChannel) {
                                    socketChannel.pipeline().addLast(new ServerHandler(listener, NettyCore.this));
                                }
                            })
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);
                    ChannelFuture future = bootstrap.bind(port).sync();
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void sendTextMsg(String msg, Client client) {
        Channel channel = client.getChannel();
        if (channel != null) {
            System.out.println("sendto:" + client.getInetAddress());
            //client is based on stream reader, append a line
            String string = msg + "\n";
            byte[] bytes = string.getBytes(CharsetUtil.UTF_8);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            channel.writeAndFlush(byteBuf);
        }
    }

    @Override
    public void shutdown(Map<String, Client> map) {
        //foreach 或者Iterator
        try {
            if (map.size() > 0) {
                for (String strUserID : map.keySet()) {
                    Client client = map.get(strUserID);
                    client.getChannel().writeAndFlush(CONTROL_TOKEN);
                    client.getChannel().close();
                }
            } else {
                System.out.println("no opened sockets");
            }
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            listener.onClosed(this);
        }

    }
}
