package com.jc;

import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;

@SpringBootApplication(scanBasePackages = "com.jc")
@EnableAsync
@Slf4j
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    //启动首页点单页面
//    @PostConstruct
    public void openBrowser() {
        try {
            String scriptPath = "C:\\scripts\\open_browser.ps1";
            String command = "powershell.exe -ExecutionPolicy Bypass -File \"" + scriptPath + "\"";
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public CommandLineRunner run(ApplicationContext ctx) {
        //启动客户端
//        try {
//            nettyClient.run();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //启动netty服务器
        return args -> {
            ChannelFuture future = ctx.getBean(ChannelFuture.class);
            if (future != null) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        future.channel().close();
                        future.channel().eventLoop().shutdownGracefully();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
                future.channel().closeFuture().sync();
            } else {
               log.error("Failed to start Netty server.");
            }
        };
    }
}
