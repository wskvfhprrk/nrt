package com.jc.config;

import com.jc.netty.client.NettyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
public class NettyClientConfig {

    @Value("${ducoIp}")
    private String host;

    @Value("${ducoPort}")
    private int port;

    private EventLoopGroup workerGroup;
    private Channel channel;

    @Bean
    public Bootstrap bootstrap() {
        workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new NettyClientHandler());
            }
        });

        return b;
    }

    @PreDestroy
    public void destroy() {
        if (channel != null) {
            channel.close();
        }
        workerGroup.shutdownGracefully();
    }

    public void connectAndSendData(String message) throws InterruptedException {
        ChannelFuture f = bootstrap().connect(host, port).sync();
        channel = f.channel();
        NettyClientHandler.sendData(channel, message);
    }
}
