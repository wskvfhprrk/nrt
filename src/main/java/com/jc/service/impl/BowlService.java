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
    private final String lanTo485;

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
        this.lanTo485 = lanTo485;
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
                    if (count > 300) { // 30秒超时
                        log.error("碗升到位超时！");
                        return;
                    }
                    ioStatus = ioDeviceService.getIoStatus();
                    split = ioStatus.split(",");
                    bowlSensor = split[1].equals(SignalLevel.HIGH.getValue());
                    if(!bowlSensor){
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
                    if (count > 300) { // 30秒超时
                        log.error("碗升到位超时！");
                        return;
                    }
                    ioStatus = ioDeviceService.getIoStatus();
                    split = ioStatus.split(",");
                    bowlSensor = split[1].equals(SignalLevel.HIGH.getValue());
                    if(bowlSensor){
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
        if (ioStatus.equals(StepperMotorConstants.NOT_INITIALIZED)) {
            log.error("无法获取传感器的值！");
            // 先重置传感器
            stepperMotorService.stop(StepperMotorConstants.BOWL_CONTROLLER_NO);
            try {
                // 等待指定时间，确保传感器完成重置
                Thread.sleep(StepperMotorConstants.SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 重新获取传感器状态
            ioStatus = ioDeviceService.getIoStatus();
        }

        // 解析传感器状态字符串
        String[] split = ioStatus.split(",");
        boolean bowlSensor = split[0].equals(SignalLevel.HIGH.getValue());
        boolean bowlSensor2 = split[1].equals(SignalLevel.HIGH.getValue());

        log.info("传感器1状态：{}", bowlSensor ? "有碗" : "无碗");
        log.info("传感器2状态：{}", bowlSensor2 ? "有碗" : "无碗");

        if (bowlSensor) {
            log.info("检测到碗已经升到位，进行降碗操作...");
            // 降碗到传感器2低电平
            lowerBowlToLowSensor();
        }
    }

    /**
     * 降低碗到传感器2低电平
     */
    private void lowerBowlToLowSensor() {
        // 获取传感器状态
        String ioStatus = ioDeviceService.getIoStatus();
        if (ioStatus.equals(StepperMotorConstants.NOT_INITIALIZED)) {
            log.error("无法获取传感器的值！");
            return;
        }

        // 解析传感器状态字符串
        String[] split = ioStatus.split(",");
        boolean bowlSensor2 = split[1].equals(SignalLevel.HIGH.getValue());

        if (!bowlSensor2) {
            log.info("传感器2检测到低电平，停止降碗");
            return;
        }

        // 调整步进电机速度
        String modificationSpeed = stepperMotorService.modificationSpeed(StepperMotorConstants.BOWL_CONTROLLER_NO, 50);
        log.info("修改速度结果：{}", modificationSpeed);

        // 发送降碗指令
        String lowerBowlCommand = stepperMotorService.startStepperMotor(StepperMotorConstants.BOWL_CONTROLLER_NO, false, 100);
        log.info("发送降碗指令结果：{}", lowerBowlCommand);

        try {
            // 等待指定时间，确保降碗操作完成
            Thread.sleep(StepperMotorConstants.SLEEP_TIME_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 再次检查传感器2状态
        lowerBowlToLowSensor();
    }
    public String bowlRising() {
        return stepperMotorService.startStepperMotor(StepperMotorConstants.BOWL_CONTROLLER_NO, false, 0);
    }

    public String bowlDescent() {
        return stepperMotorService.startStepperMotor(StepperMotorConstants.BOWL_CONTROLLER_NO, true, 0);
    }

}
