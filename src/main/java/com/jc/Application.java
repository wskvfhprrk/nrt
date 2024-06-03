package com.jc;

import io.netty.channel.ChannelFuture;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.IOException;

@SpringBootApplication(scanBasePackages = "com.jc")
@EnableAsync
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
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
                System.err.println("Failed to start Netty server.");
            }
        };
    }
}
