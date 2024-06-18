package com.jc.controller;

import com.jc.config.NettyClientConfig;
import com.jc.service.impl.BowlService;
import com.jc.service.impl.TurntableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TotelController {
    @Autowired
    NettyClientConfig nettyClientConfig;
    @Autowired
    BowlService bowlService;
    @Autowired
    TurntableService turntableService;


    @GetMapping("reset")
    public String reset() throws Exception {
        //机器人重置命令
        new Thread(() -> {
            try {
                nettyClientConfig.connectAndSendData("run(reset.jspf)");
                log.info("机器人重置命令");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).run();
        //转盘重置
        turntableService.turntableReset();
        log.info("转盘重置");
        //碗重置
        bowlService.bowlReset();
        log.info("碗重置");
        return "ok";
    }

    @GetMapping("takeBowl")
    public String takeBowl() {
        // TODO: 2024/6/12 判断是否回原点、碗的状态、 
        //升碗
        new Thread(() -> {
            bowlService.continuousBowlCheck();
        }).run();
        //向机器人发送取碗指令
        try {
            nettyClientConfig.connectAndSendData("run(takeABowl.jspf)");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "ok";
    }

    @GetMapping("putBowl")
    public String putBowl(){
        try {
            nettyClientConfig.connectAndSendData("run(putBowl.jspf)");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "ok";
    }
}
