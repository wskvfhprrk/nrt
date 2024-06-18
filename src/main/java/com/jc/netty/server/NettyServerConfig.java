package com.jc.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Netty 服务器配置类
 * 配置并启动 Netty 服务器
 */
@Configuration
@Slf4j
public class NettyServerConfig {

    @Value("${netty.port}")
    private int port;

    /**
     * 配置并启动 Netty 服务器
     *
     * @param nettyServerHandler 处理客户端连接的处理器
     * @return 返回服务器的 ChannelFuture
     */
    @Bean
    public ChannelFuture serverBootstrap(NettyServerHandler nettyServerHandler) {
        // 创建bossGroup和workerGroup线程池
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            // 配置服务器参数
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            // 将自定义的处理器添加到通道的处理器链中
                            ch.pipeline().addLast(nettyServerHandler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置队列大小
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // 保持长连接

            // 绑定端口并启动服务器
            ChannelFuture f = b.bind(port).sync();

            // 记录服务器启动日志
            log.info("Netty 服务器已启动，端口号：{}",port);

            return f;
        } catch (InterruptedException e) {
            log.error("服务器启动失败", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
