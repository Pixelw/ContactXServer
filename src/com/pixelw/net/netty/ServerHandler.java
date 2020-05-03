package com.pixelw.net.netty;

import com.pixelw.entity.Client;
import com.pixelw.net.ServerCore;
import com.pixelw.net.ServerListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Carl Su
 * @date 2020/5/1
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private ServerListener listener;
    private NettyCore nettyCore;

    public ServerHandler(ServerListener listener, NettyCore nettyCore) {
        super();
        this.listener = listener;
        this.nettyCore = nettyCore;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            ByteBuf in = (ByteBuf) msg;
            String string = in.toString(CharsetUtil.UTF_8);
            String trimmed = string.substring(0, string.length() - 1);
            if (!trimmed.equals(ServerCore.CONTROL_TOKEN)) {
                listener.onMessage(nettyCore, new Client(ctx.channel()), trimmed);
            } else {
                ctx.close();
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
