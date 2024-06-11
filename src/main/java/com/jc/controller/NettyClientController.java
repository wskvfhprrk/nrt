package com.jc.controller;

import com.jc.config.NettyClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NettyClientController {
    @Autowired
    NettyClientConfig nettyClientConfig;

    @GetMapping("sendMessage")
    public String sendMessage(){
        //zhongwan jspf
        try {
            nettyClientConfig.connectAndSendData("run(zhongwan.jspf)");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "ok";
    }
}
