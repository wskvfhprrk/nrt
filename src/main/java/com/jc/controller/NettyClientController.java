package com.jc.controller;

import com.jc.config.NettyClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NettyClientController {
    @Autowired
    NettyClientConfig nettyClientConfig;

    @GetMapping("takeABowl")
    public String takeABowl(){
        try {
            nettyClientConfig.connectAndSendData("run(takeABowl.jspf)");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "ok";
    }
}
