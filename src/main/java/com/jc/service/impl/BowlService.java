package com.jc.service.impl;

import com.jc.constants.StepperMotorConstants;
import com.jc.enums.SignalLevel;
import com.jc.netty.server.NettyServerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 碗控制服务实现类，负责碗的升降操作和状态检查
 */
@Service
@Slf4j
public class BowlService {

    private final StepperMotorService stepperMotorService;
    private final IODeviceService ioDeviceService;

    @Value("${IoIp}")
    private String ioIp;
    @Autowired
    private NettyServerHandler nettyServerHandler;

    @Autowired
    public BowlService(StepperMotorService stepperMotorService,
                       IODeviceService ioDeviceService,
                       @Value("${lanTo485}") String lanTo485) {
        this.stepperMotorService = stepperMotorService;
        this.ioDeviceService = ioDeviceService;
    }

    /**
     * 重置碗
     */
    public void bowlReset() {
        // 获取传感器状态
        String ioStatus = ioDeviceService.getIoStatus();
        while (ioStatus.equals(StepperMotorConstants.NOT_INITIALIZED)) {
            log.error("无法获取传感器的值！");
            // 先重置传感器
            nettyServerHandler.sendMessageToClient(ioIp, StepperMotorConstants.RESET_COMMAND, true);
            try {
                // 等待指定时间，确保传感器完成重置
                Thread.sleep(StepperMotorConstants.SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 重新获取传感器状态
            ioStatus = ioDeviceService.getIoStatus();
            if (ioStatus.equals(StepperMotorConstants.NOT_INITIALIZED)) {
                log.error("没有发现传感器的值！");
            }
        }

        // 解析传感器状态字符串
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
                    Thread.sleep(StepperMotorConstants.SLEEP_TIME_MS);
                    count++;
//                    if (count > 300) { // 30秒超时
//                        log.error("碗升到位超时！");
//                        return;
//                    }
                    ioStatus = ioDeviceService.getIoStatus();
                    split = ioStatus.split(",");
                    bowlSensor = split[1].equals(SignalLevel.HIGH.getValue());
                    if (!bowlSensor) {
                        stepperMotorService.stop(StepperMotorConstants.BOWL_CONTROLLER_NO);
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
                    Thread.sleep(StepperMotorConstants.SLEEP_TIME_MS);
                    count++;
//                    if (count > 300) { // 30秒超时
//                        log.error("碗升到位超时！");
//                        return;
//                    }
                    ioStatus = ioDeviceService.getIoStatus();
                    split = ioStatus.split(",");
                    bowlSensor = split[1].equals(SignalLevel.HIGH.getValue());
                    if (bowlSensor) {
                        stepperMotorService.stop(StepperMotorConstants.BOWL_CONTROLLER_NO);
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
        String ioStatus = ioDeviceService.getIoStatus();
        while (ioStatus.equals(StepperMotorConstants.NOT_INITIALIZED)) {
            log.error("无法获取传感器的值！");
            // 先重置传感器
            nettyServerHandler.sendMessageToClient(ioIp, StepperMotorConstants.RESET_COMMAND, true);
            try {
                // 等待指定时间，确保传感器完成重置
                Thread.sleep(StepperMotorConstants.SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 重新获取传感器状态
            ioStatus = ioDeviceService.getIoStatus();
            if (ioStatus.equals(StepperMotorConstants.NOT_INITIALIZED)) {
                log.error("没有获取到传感器的值！");
            }
        }

        // 解析传感器状态字符串
        String[] split = ioStatus.split(",");
        boolean bowlSensor = split[1].equals(SignalLevel.HIGH.getValue()); // 碗传感器状态
        boolean lowerLimit = split[2].equals(SignalLevel.HIGH.getValue()); // 轨道最低极限点状态
        boolean upperLimit = split[3].equals(SignalLevel.HIGH.getValue()); // 轨道最高极限点状态
        //如果传感器无值到达了上限——没有碗了
        if (!bowlSensor && upperLimit){
            log.error("没有碗了！");
            return;
        }
        if (!bowlSensor && !upperLimit) {
            this.bowlRising();
            // 等待传感器2变为高电平，最多等待30秒
            int count = 0;
            while (!bowlSensor) {
                try {
                    Thread.sleep(StepperMotorConstants.SLEEP_TIME_MS);
                    count++;
//                    if (count > 300) { // 30秒超时
//                        log.error("碗升到位超时！");
//                        return;
//                    }
                    ioStatus = ioDeviceService.getIoStatus();
                    split = ioStatus.split(",");
                    bowlSensor = split[1].equals(SignalLevel.HIGH.getValue());
                    if (bowlSensor) {
                        stepperMotorService.stop(StepperMotorConstants.BOWL_CONTROLLER_NO);
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
        return stepperMotorService.startStepperMotor(StepperMotorConstants.BOWL_CONTROLLER_NO, false, 0);
    }

    public String bowlDescent() {
        return stepperMotorService.startStepperMotor(StepperMotorConstants.BOWL_CONTROLLER_NO, true, 0);
    }

}
