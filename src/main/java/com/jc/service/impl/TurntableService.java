package com.jc.service.impl;

import com.jc.constants.Constants;
import com.jc.enums.SignalLevel;
import com.jc.netty.server.NettyServerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 转台步进电机控制器
 */
@Service
@Slf4j
public class TurntableService {
    @Autowired
    private RedisTemplate redisTemplate;
    private final NettyServerHandler nettyServerHandler;
    private final String lanTo485;
    @Value("${IoIp}")
    private String ioIp;
    private final StepperMotorService stepperMotorService;

    @Autowired
    public TurntableService(NettyServerHandler nettyServerHandler,
                            @Value("${lanTo485}") String lanTo485, StepperMotorService stepperMotorService) {
        this.nettyServerHandler = nettyServerHandler;
        this.lanTo485 = lanTo485;
        this.stepperMotorService = stepperMotorService;
    }

    /**
     * 碗回原点
     *
     * @return
     */
    public String turntableReset() {
        // 获取传感器状态
        Object o = redisTemplate.opsForValue().get(Constants.IO_KEY);
        while (o==null) {
            log.error("无法获取传感器的值！");
            // 先重置传感器
            nettyServerHandler.sendMessageToClient(ioIp, Constants.RESET_COMMAND, true);
            try {
                // 等待指定时间，确保传感器完成重置
                Thread.sleep(Constants.SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 重新获取传感器状态
            o = redisTemplate.opsForValue().get(Constants.IO_KEY);
        }
        if (String.valueOf(o).split(",")[0].equals(SignalLevel.HIGH)) {
            log.info("转盘已经在原点位置！");
            return "ok";
        }
        if (String.valueOf(o).split(",")[0].equals(SignalLevel.LOW.getValue())) {
            //发送转动转盘指令至到为高电平
            stepperMotorService.startStepperMotor(3, true, 0);
            Boolean flag = true;
            while (flag) {
                Object o1 = redisTemplate.opsForValue().get(Constants.IO_KEY);
                if (String.valueOf(o1).equals(SignalLevel.HIGH.getValue())) {
                    stepperMotorService.stop(3);
                    flag = false;
                }
                try {
                    Thread.sleep(Constants.SLEEP_TIME_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return "ok";
    }

    public String feeding(){
        //先复位
        Object o = redisTemplate.opsForValue().get(Constants.IO_KEY);
        while (o==null) {
            // 先重置传感器
            nettyServerHandler.sendMessageToClient(ioIp, Constants.RESET_COMMAND, true);
            try {
                // 等待指定时间，确保传感器完成重置
                Thread.sleep(Constants.SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 重新获取传感器状态
            o = redisTemplate.opsForValue().get(Constants.IO_KEY);
        }
        if (String.valueOf(o).split(",")[0].equals(SignalLevel.HIGH.getValue())) {
            stepperMotorService.startStepperMotor(3,true,1600);
            return "ok";
        }
        if (String.valueOf(o).split(",")[0].equals(SignalLevel.LOW.getValue())) {
            //发送转动转盘指令至到为高电平
            stepperMotorService.startStepperMotor(3, true, 0);
            Boolean flag = true;
            while (flag) {
                Object o1 = redisTemplate.opsForValue().get(Constants.IO_KEY);
                if (String.valueOf(o1).equals(SignalLevel.HIGH.getValue())) {
                    //步进电机转半圈
                    stepperMotorService.startStepperMotor(3,true,1600);
                    flag = false;
                }
                try {
                    Thread.sleep(Constants.SLEEP_TIME_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return "ok";
    }
}
