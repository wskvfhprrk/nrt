package com.jc.service.impl;

import com.jc.constants.StepperMotorConstants;
import com.jc.enums.SignalLevel;
import com.jc.netty.server.NettyServerHandler;
import com.jc.utils.CRC16;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 步进电机服务实现类，负责控制步进电机的运行、速度调整和停止
 */
@Service
@Slf4j
public class StepperMotor {

    @Autowired
    private NettyServerHandler nettyServerHandler;

    @Value("${lanTo485}")
    private String lanTo485;

    @Value("${IoIp}")
    private String ioIp;

    private IODeviceHandler ioDeviceHandler;

    @Autowired
    public StepperMotor(IODeviceHandler ioDeviceHandler) {
        this.ioDeviceHandler = ioDeviceHandler;
    }

    public String bowlRising() {
        return this.startStepperMotor(StepperMotorConstants.BOWL_CONTROLLER_NO, false, 0);
    }

    public String bowlDescent() {
        return this.startStepperMotor(StepperMotorConstants.BOWL_CONTROLLER_NO, true, 0);
    }
    /**
     * 执行初始化检测或连续出碗检查的方法。
     *
     * @param isInitialization 如果为 true，执行初始化检测；如果为 false，执行连续出碗检查。
     */
    public void check(boolean isInitialization) {
        if (isInitialization) {
            // 初始化检测
            bowlReset();
        } else {
            // 连续出碗检查
            continuousBowlCheck();
        }
    }

    /**
     * 连续出碗检查方法，用于检测碗是否已经升到位，并在需要时进行降碗操作直到碗低出传感器为止。
     */
    private void continuousBowlCheck() {
        // 获取传感器状态
        String ioStatus = ioDeviceHandler.getIoStatus();
        if (ioStatus.equals(StepperMotorConstants.NOT_INITIALIZED)) {
            log.error("无法获取传感器的值！");
            return;
        }
        // 解析传感器状态字符串
        String[] split = ioStatus.split(",");
        boolean bowlSensor = split[1].equals(SignalLevel.HIGH.getValue()); // 碗传感器状态

        // 如果传感器2为高电平，说明碗已经升到位
        if (bowlSensor) {
            // 如果同时轨道最低极限点为低电平，降碗直到碗低出传感器为止
            if (!split[2].equals(SignalLevel.HIGH.getValue())) {
                this.bowlDescent();
                // 循环等待传感器2变为低电平
                while (true) {
                    String newIoStatus = ioDeviceHandler.getIoStatus();
                    if (newIoStatus.split(",")[1].equals(SignalLevel.LOW.getValue())) {
                        this.stop(StepperMotorConstants.BOWL_CONTROLLER_NO);
                        break;
                    }
                    try {
                        Thread.sleep(StepperMotorConstants.SLEEP_TIME_MS); // 每100毫秒检查一次
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                log.info("碗已经升到位，无需进行升碗操作！");
            }
            return;
        }
        // 如果传感器2为低电平，说明碗还未升到位
        log.error("碗未升到位，请检查传感器2状态！");
    }

    /**
     * 重置碗
     */
    public void bowlReset() {
        // 获取传感器状态
        String ioStatus = ioDeviceHandler.getIoStatus();
        if (ioStatus.equals(StepperMotorConstants.NOT_INITIALIZED)) {
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
            ioStatus = ioDeviceHandler.getIoStatus();
        }

        // 解析传感器状态字符串
        String[] split = ioStatus.split(",");
        boolean bowlSensor = split[1].equals(SignalLevel.HIGH.getValue()); // 碗传感器状态
        boolean lowerLimit = split[2].equals(SignalLevel.HIGH.getValue()); // 轨道最低极限点状态
        boolean upperLimit = split[3].equals(SignalLevel.HIGH.getValue()); // 轨道最高极限点状态

        // 如果传感器2为高电平，说明碗已经升到位
        if (bowlSensor) {
            // 如果同时轨道最低极限点为低电平，降碗直到碗低出传感器为止
            if (!lowerLimit) {
                this.bowlDescent();
                // 循环等待传感器2变为低电平
                while (true) {
                    String newIoStatus = ioDeviceHandler.getIoStatus();
                    if (newIoStatus.split(",")[1].equals(SignalLevel.LOW.getValue())) {
                        this.stop(StepperMotorConstants.BOWL_CONTROLLER_NO);
                        break;
                    }
                    try {
                        Thread.sleep(StepperMotorConstants.SLEEP_TIME_MS); // 每100毫秒检查一次
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                log.info("碗已经升到位，无需进行升碗操作！");
            }
            return;
        }

        // 如果2、3、4传感器都为低电平，直接升碗
        if (!lowerLimit && !upperLimit) {
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
                    ioStatus = ioDeviceHandler.getIoStatus();
                    split = ioStatus.split(",");
                    bowlSensor = split[1].equals(SignalLevel.HIGH.getValue());
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
     * 启动步进电机
     *
     * @param no                 步进电机编号
     * @param positiveOrNegative 步进电机转动方向，true表示正转，false表示反转
     * @param numberOfPulses     脉冲数量
     */
    public String startStepperMotor(int no, Boolean positiveOrNegative, int numberOfPulses) {
        if (no <= 0 || no > StepperMotorConstants.MAX_MOTOR_NO) {
            log.error(StepperMotorConstants.ERROR_NO_MOTOR, no);
            return String.format(StepperMotorConstants.ERROR_NO_MOTOR, no); // 添加return，防止继续执行
        }

        // 检查传感器状态以决定是否可以启动电机
        String[] split = ioDeviceHandler.getIoStatus().split(",");
        if (SignalLevel.fromValue(split[2]) == SignalLevel.HIGH && no == StepperMotorConstants.BOWL_CONTROLLER_NO && positiveOrNegative) {
            log.error(StepperMotorConstants.ERROR_BOWL_LOWEST);
            return StepperMotorConstants.ERROR_BOWL_LOWEST;
        }
        if (SignalLevel.fromValue(split[3]) == SignalLevel.HIGH && no == StepperMotorConstants.BOWL_CONTROLLER_NO && !positiveOrNegative) {
            log.error(StepperMotorConstants.ERROR_BOWL_HIGHEST);
            return StepperMotorConstants.ERROR_BOWL_HIGHEST;
        }

        // 先发脉冲
        String noStr = String.format("%02X", no);
        String pulseStr = String.format("%04X", numberOfPulses).toUpperCase();
        String command = noStr + "060007" + pulseStr;
        String crc = CRC16.getModbusrtuString(command);
        command += crc;
        log.info("脉冲指令：{}", command);
        nettyServerHandler.sendMessageToClient(lanTo485, command, true);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 发送正反转指令
        String direction = positiveOrNegative ? "00" : "01";
        String directionCommand = noStr + "060000" + direction + "0001";
        crc = CRC16.getModbusrtuString(directionCommand);
        directionCommand += crc;
        log.info("步进电机转动指令：{}", directionCommand);
        nettyServerHandler.sendMessageToClient(lanTo485, directionCommand, true);
        return "ok";
    }

    /**
     * 修改步进电机速度
     *
     * @param no    步进电机编号
     * @param speed 步进电机速度
     */
    public String modificationSpeed(int no, int speed) {
        if (no <= 0 || no > StepperMotorConstants.MAX_MOTOR_NO) {
            log.error(StepperMotorConstants.ERROR_NO_MOTOR, no);
            return "步进电机编号不存在"; // 添加return，防止继续执行
        }
        if (speed >= StepperMotorConstants.MAX_SPEED) {
            log.error("设置速度：{}超过最大速度{}了", speed, StepperMotorConstants.MAX_SPEED);
            return StepperMotorConstants.ERROR_SPEED_TOO_HIGH;
        }

        String noStr = String.format("%02X", no);
        String speedStr = String.format("%04X", speed).toUpperCase();
        String command = noStr + "060005" + speedStr;
        String crc = CRC16.getModbusrtuString(command);
        command += crc;
        log.info("步进电机速度指令：{}", command);
        nettyServerHandler.sendMessageToClient(lanTo485, command, true);
        return "ok";
    }

    /**
     * 停止步进电机
     *
     * @param no 步进电机编号
     */
    public void stop(int no) {
        if (no <= 0 || no > StepperMotorConstants.MAX_MOTOR_NO) {
            log.error(StepperMotorConstants.ERROR_NO_MOTOR, no);
            return; // 添加return，防止继续执行
        }

        String noStr = String.format("%02X", no);
        String command = noStr + "0600020001";
        String crc = CRC16.getModbusrtuString(command);
        command += crc;
        nettyServerHandler.sendMessageToClient(lanTo485, command, true);
    }
}
