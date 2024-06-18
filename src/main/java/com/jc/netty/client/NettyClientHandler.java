package com.jc.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    public static void sendData(Channel channel, String data) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(data.getBytes());
        channel.writeAndFlush(buf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 读取服务端响应
        ByteBuf buf = (ByteBuf) msg;
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        String response = new String(data);
        System.out.println("Server response: " + response);
        ctx.close(); // 关闭连接
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        cause.printStackTrace();
        ctx.close();
    }
}
