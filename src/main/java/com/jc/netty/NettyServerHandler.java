package com.jc.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Map<String, Channel> clientMap = new ConcurrentHashMap<>();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        InetSocketAddress clientAddress = (InetSocketAddress) channel.remoteAddress();
        clientMap.put(clientAddress.getAddress().toString().replace("/", ""), channel);
        channels.add(channel);
        log.info("客户端连接成功，IP地址为：{}", clientAddress.getAddress().getHostAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        InetSocketAddress clientAddress = (InetSocketAddress) channel.remoteAddress();
        clientMap.remove(clientAddress.getAddress().toString().replace("/", ""));
        channels.remove(channel);
        log.info("客户端断开连接，IP地址为：{}", clientAddress.getAddress().getHostAddress());
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InetSocketAddress clientAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = clientAddress.getAddress().toString().replace("/", "");
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            String message = byteBuf.toString(Charset.defaultCharset());
            log.info("接收到来自 {} 的消息: {}", clientIp, message);
            // 释放 ByteBuf 资源
            byteBuf.release();
        } else {
            log.error("接收到的消息类型不是 ByteBuf！");
        }

    }

    public void sendMessageToClient(String clientIp, String message) {
        for (Map.Entry<String, Channel> entry : clientMap.entrySet()) {
            String address = entry.getKey();
            if (address.equals(clientIp)) {
                Channel channel = entry.getValue();
                // 将消息转换为 ByteBuf 类型
                ByteBuf bufff = Unpooled.buffer();
                bufff.writeBytes(message.getBytes());
                channel.writeAndFlush(bufff);
                bufff.release();
                return;
            }
            log.error("无法找到与 IP 地址 {} 相关联的通道！", clientIp);
        }
    }
}
