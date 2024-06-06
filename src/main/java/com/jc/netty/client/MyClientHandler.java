package com.jc.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class MyClientHandler extends SimpleChannelInboundHandler<Object> {

    private Channel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        String message = byteBuf.toString(Charset.defaultCharset());
        log.info("接收的消息: {}", message);
        // 释放 ByteBuf 资源
        releaseBuffer(byteBuf);
    }

    /**
     * 当buffer.refCnt()!=1时释放ByteBuf
     * @param buffer
     */
    private void releaseBuffer(ByteBuf buffer) {
        if (buffer != null && buffer.refCnt()!=1) {
            buffer.release(); // 释放 ByteBuf 资源
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 发送消息给服务器
     * @param message 消息内容
     */
    public void sendMessage(String message) {
        if (channel != null && channel.isActive()) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(message, Charset.defaultCharset());
            channel.writeAndFlush(byteBuf);
        } else {
            log.error("无法发送消息，连接尚未建立或已关闭！");
        }
    }
}
