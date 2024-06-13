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
 * 碗控制服务实现类，负责碗的升降操作和状态检查
 */
@Service
@Slf4j
public class BowlService {

    private final StepperMotorService stepperMotorService;

    @Value("${IoIp}")
    private String ioIp;
    @Autowired
    private NettyServerHandler nettyServerHandler;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    public BowlService(StepperMotorService stepperMotorService,
                       @Value("${lanTo485}") String lanTo485) {
        this.stepperMotorService = stepperMotorService;
    }

    /**
     * 重置碗
     */
    public void bowlReset() {
        // 获取传感器状态
        Object o = redisTemplate.opsForValue().get(Constants.IO_KEY);
        while (o == null) {
            log.error("无法获取传感器的值！");
            // 先重置传感器
            nettyServerHandler.sendMessageToClient(ioIp, Constants.RESET_COMMAND, true);
            try {
                // 等待指定时间，确保传感器完成重置
                Thread.sleep(Constants.SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            o = redisTemplate.opsForValue().get(Constants.IO_KEY);
        }

        // 解析传感器状态字符串
        String ioStatus = String.valueOf(o);
        String[] split = ioStatus.split(",");
        boolean bowlSensor = split[1].equals(SignalLevel.HIGH.getValue()); // 碗传感器状态
        boolean lowerLimit = split[2].equals(SignalLevel.HIGH.getValue()); // 轨道最低极限点状态
        boolean upperLimit = split[3].equals(SignalLevel.HIGH.getValue()); // 轨道最高极限点状态

        // 如果2为高电平4为低电平，直接降碗
        if (bowlSensor && !lowerLimit) {
            this.bowlDescent();
            // 等待传感器2变为高电平，最多等待30秒
            int count = 0;
            while (bowlSensor) {
                try {
                    Thread.sleep(Constants.SLEEP_TIME_MS);
                    count++;
                    Object o1 = redisTemplate.opsForValue().get(Constants.IO_KEY);
                    ioStatus = String.valueOf(o1);
                    split = ioStatus.split(",");
                    bowlSensor = split[1].equals(SignalLevel.HIGH.getValue());
                    if (!bowlSensor) {
                        stepperMotorService.stop(Constants.BOWL_CONTROLLER_NO);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("碗已经降到位！");
            return;
        }
        // 如果2、4传感器都为低电平，直接升碗
        if (!bowlSensor && !upperLimit) {
            this.bowlRising();
            // 等待传感器2变为高电平，最多等待30秒
            int count = 0;
            while (!bowlSensor) {
                try {
                    Thread.sleep(Constants.SLEEP_TIME_MS);
                    count++;
                    Object o1 = redisTemplate.opsForValue().get(Constants.IO_KEY);

                    ioStatus = String.valueOf(o1);
                    split = ioStatus.split(",");
                    bowlSensor = split[1].equals(SignalLevel.HIGH.getValue());
                    if (bowlSensor) {
                        stepperMotorService.stop(Constants.BOWL_CONTROLLER_NO);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("碗已经升到位！");
            return;
        }

        // 如果传感器2为低电平，说明碗还未升到位
        log.error("碗未升到位，请检查传感器2状态！");
    }

    /**
     * 连续出碗检查方法，用于检测碗是否已经升到位，并在需要时进行降碗操作直到碗低出传感器为止。
     */
    public void continuousBowlCheck() {
        // 获取传感器状态
        Object o = redisTemplate.opsForValue().get(Constants.IO_KEY);
        while (o == null){
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

        // 解析传感器状态字符串
        String[] split = String.valueOf(o).split(",");
        boolean bowlSensor = split[1].equals(SignalLevel.HIGH.getValue()); // 碗传感器状态
        boolean lowerLimit = split[2].equals(SignalLevel.HIGH.getValue()); // 轨道最低极限点状态
        boolean upperLimit = split[3].equals(SignalLevel.HIGH.getValue()); // 轨道最高极限点状态
        //如果传感器无值到达了上限——没有碗了
        if (!bowlSensor && upperLimit) {
            log.error("没有碗了！");
            return;
        }
        if (!bowlSensor && !upperLimit) {
            this.bowlRising();
            // 等待传感器2变为高电平，最多等待30秒
            int count = 0;
            while (!bowlSensor) {
                try {
                    Thread.sleep(Constants.SLEEP_TIME_MS);
                    count++;
                    Object o1 = redisTemplate.opsForValue().get(Constants.IO_KEY);
                    split = String.valueOf(o1).split(",");
                    bowlSensor = split[1].equals(SignalLevel.HIGH.getValue());
                    if (bowlSensor) {
                        stepperMotorService.stop(Constants.BOWL_CONTROLLER_NO);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("碗已经升到位！");
        }
        log.info("碗已经升到位！");
    }

    public String bowlRising() {
        return stepperMotorService.startStepperMotor(Constants.BOWL_CONTROLLER_NO, false, 0);
    }

    public String bowlDescent() {
        return stepperMotorService.startStepperMotor(Constants.BOWL_CONTROLLER_NO, true, 0);
    }

}
