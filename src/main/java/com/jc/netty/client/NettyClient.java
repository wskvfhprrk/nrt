//package com.jc.netty.client;
//
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component
//public class NettyClient {
//    @Value("${ducoIp}")
//    private String host;
//    @Value("${ducoPort}")
//    private int port;
//
//    public NettyClient(String host, int port) {
//        this.host = host;
//        this.port = port;
//    }
//
//    public void run() throws Exception {
//        EventLoopGroup group = new NioEventLoopGroup();
//        try {
//            Bootstrap b = new Bootstrap();
//            b.group(group)
//                    .channel(NioSocketChannel.class)
//                    .option(ChannelOption.TCP_NODELAY, true)
//                    .handler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        public void initChannel(SocketChannel ch) throws Exception {
//                            ch.pipeline().addLast(new MyClientHandler());
//                        }
//                    });
//
//            // 启动客户端.
//            Channel channel = b.connect(host, port).sync().channel();
//
//            // Wait until the connection is closed.
//            channel.closeFuture().sync();
//        } finally {
//            // Shut down the event loop to terminate all threads.
//            group.shutdownGracefully();
//        }
//    }
//
//    public static void main(String[] args) throws Exception {
//        String host = "127.0.0.1";
//        int port = 4000; // your server port
//        new NettyClient(host, port).run();
//    }
//}
