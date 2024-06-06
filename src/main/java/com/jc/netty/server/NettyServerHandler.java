package com.jc.netty.server;

import com.jc.service.impl.IODeviceHandler;
import com.jc.service.impl.RelayDeviceHandler;
import com.jc.utils.HexConvert;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ChannelHandler.Sharable
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    //客户端map集合
    private static final Map<String, Channel> clientMap = new ConcurrentHashMap<>();
    @Autowired
    private FicationProcessing ficationProcessing;

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
            // 获取可读字节数
            int readableBytes = byteBuf.readableBytes();
            // 创建一个字节数组来存储可读的字节
            byte[] bytes = new byte[readableBytes];
            byteBuf.readBytes(bytes);
            // 处理字符串
            // 将字节数组转换为字符串
            String s = HexConvert.BinaryToHexString(bytes);
            boolean hexStringWithSpaces = isHexStringWithSpaces(s);
            // 检查字节数组是否以 "H:" 开头——HEX字符串
            if (hexStringWithSpaces) {
                String hexString = HexConvert.BinaryToHexString(bytes);
                log.info("clientIp：{}发送的HEX字符:{}", clientIp, hexString);
                ficationProcessing.classificationProcessing(clientIp, true, hexString);
            } else {
                String str = new String(bytes, StandardCharsets.UTF_8);
                log.info("clientIp：{}发送的普通字符串：{}", clientIp, str);
                ficationProcessing.classificationProcessing(clientIp, false, str);
            }
            releaseBuffer(byteBuf);
            // 分类处理器处理


        } else {
            super.channelRead(ctx, msg);
        }
    }

    /**
     * 判断字符串是否是16进制字符串
     *
     * @param str
     * @return
     */
    private boolean isHexStringWithSpaces(String str) {
        // 检查字符串是否为空
        if (str == null || str.isEmpty()) {
            return false;
        }
        // 检查字符串中的每个字符是否为十六进制字符或空格
        for (char ch : str.toCharArray()) {
            if (!((ch >= '0' && ch <= '9') ||
                    (ch >= 'a' && ch <= 'f') ||
                    (ch >= 'A' && ch <= 'F') ||
                    ch == ' ')) {
                return false;
            }
        }
        // 字符串合法，包含十六进制字符和空格
        return true;
    }


    public void sendMessageToClient(String clientIp, String message, Boolean hex) {
        for (Map.Entry<String, Channel> entry : clientMap.entrySet()) {
            String address = entry.getKey();
            if (address.equals(clientIp)) {
                Channel channel = entry.getValue();
                if (hex) {
                    ByteBuf bufff = Unpooled.buffer();
                    bufff.writeBytes(HexConvert.hexStringToBytes(message.replaceAll(" ","")));
                    channel.writeAndFlush(bufff);
                } else {
                    ByteBuf bufff = Unpooled.buffer();
                    bufff.writeBytes(message.getBytes());
                    channel.writeAndFlush(bufff);
                }
                return;
            }
        }
        log.error("无法找到与 IP 地址 {} 相关联的通道！", clientIp);
    }

    /**
     * 当buffer.refCnt()!=1时释放ByteBuf
     *
     * @param buffer
     */
    private void releaseBuffer(ByteBuf buffer) {
        if (buffer != null && buffer.refCnt() != 1) {
            buffer.release(); // 释放 ByteBuf 资源
        }
    }
}
