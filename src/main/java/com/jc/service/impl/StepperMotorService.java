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
public class StepperMotorService {


    private final NettyServerHandler nettyServerHandler;
    private final IODeviceService ioDeviceService;
    private final String lanTo485;
    @Value("${IoIp}")
    private String ioIp;

    @Autowired
    public StepperMotorService(NettyServerHandler nettyServerHandler,
                               IODeviceService ioDeviceService,
                               @Value("${lanTo485}") String lanTo485) {
        this.nettyServerHandler = nettyServerHandler;
        this.ioDeviceService = ioDeviceService;
        this.lanTo485 = lanTo485;
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
        String ioStatus = ioDeviceService.getIoStatus();
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
                    String newIoStatus = ioDeviceService.getIoStatus();
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
        String ioStatus = ioDeviceService.getIoStatus();
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
            ioStatus = ioDeviceService.getIoStatus();
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
                    String newIoStatus = ioDeviceService.getIoStatus();
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

        // 如果2、4传感器都为低电平，直接升碗
        if (!bowlSensor && !upperLimit) {
            this.bowlRising();
            // 等待传感器2变为高电平，最多等待30秒
            int count = 0;
            while (!bowlSensor) {
                try {
                    Thread.sleep(StepperMotorConstants.SLEEP_TIME_MS);
                    count++;
                    log.info("count:{}", count);
                    if (count > 300) { // 30秒超时
                        this.stop(2);
                        log.error("碗升到位超时！");
                        return;
                    }
                    ioStatus = ioDeviceService.getIoStatus();
                    split = ioStatus.split(",");
                    bowlSensor = split[1].equals(SignalLevel.HIGH.getValue());
                    if (bowlSensor) {
                        this.stop(2);
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

    public String bowlRising() {
        return startStepperMotor(2, false, 0);
    }

    public String bowlDescent() {
        return startStepperMotor(2, true, 0);
    }

    /**
     * 启动步进电机
     *
     * @param motorNumber        步进电机编号
     * @param positiveOrNegative 步进电机转动方向，true表示正转，false表示反转
     * @param numberOfPulses     脉冲数量
     */
    public String startStepperMotor(int motorNumber, boolean positiveOrNegative, int numberOfPulses) {
        if (motorNumber <= 0 || motorNumber > StepperMotorConstants.MAX_MOTOR_NO) {
            log.error("编号{}步进电机不存在！", motorNumber);
            return "编号" + motorNumber + "步进电机不存在";
        }

        String[] ioStatus = ioDeviceService.getIoStatus().split(",");
        if (isMotorBlocked(ioStatus, motorNumber, positiveOrNegative)) {
            String errorMessage = getBlockErrorMessage(motorNumber, positiveOrNegative);
            log.error(errorMessage);
            return errorMessage;
        }

        sendPulseCommand(motorNumber, numberOfPulses);
        sendRotationCommand(motorNumber, positiveOrNegative);
        return "ok";
    }

    private boolean isMotorBlocked(String[] ioStatus, int motorNumber, boolean positiveOrNegative) {
        return (ioStatus[2].equals(SignalLevel.HIGH) && motorNumber == 2 && positiveOrNegative) ||
                (ioStatus[3].equals(SignalLevel.HIGH) && motorNumber == 2 && !positiveOrNegative);
    }

    private String getBlockErrorMessage(int motorNumber, boolean positiveOrNegative) {
        if (positiveOrNegative) {
            return "碗已经到最低位，不能再向下走了！";
        } else {
            return "碗已经到最高位，不能再向上走了！";
        }
    }

    private void sendPulseCommand(int motorNumber, int numberOfPulses) {
        String command = buildPulseCommand(motorNumber, numberOfPulses);
        log.info("脉冲指令：{}", command);
        nettyServerHandler.sendMessageToClient(lanTo485, command, true);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            log.error("InterruptedException during pulse command execution", e);
            Thread.currentThread().interrupt();
        }
    }

    private String buildPulseCommand(int motorNumber, int numberOfPulses) {
        String motorHex = formatMotorNumber(motorNumber);
        String pulseHex = String.format("%04X", numberOfPulses).toUpperCase();
        String commandWithoutCRC = motorHex + "060007" + pulseHex;
        String crc = CRC16.getModbusrtuString(commandWithoutCRC);
        return commandWithoutCRC + crc;
    }

    private void sendRotationCommand(int motorNumber, boolean positiveOrNegative) {
        String command = buildRotationCommand(motorNumber, positiveOrNegative);
        log.info("步进电机转动指令：{}", command);
        nettyServerHandler.sendMessageToClient(lanTo485, command, true);
    }

    private String buildRotationCommand(int motorNumber, boolean positiveOrNegative) {
        String motorHex = formatMotorNumber(motorNumber);
        String directionHex = positiveOrNegative ? "00" : "01";
        String commandWithoutCRC = motorHex + "0600" + directionHex + "0001";
        String crc = CRC16.getModbusrtuString(commandWithoutCRC);
        return commandWithoutCRC + crc;
    }

    private String formatMotorNumber(int motorNumber) {
        return String.format("%02X", motorNumber).toUpperCase();
    }

    /**
     * 修改步进电机速度
     *
     * @param motorNumber 步进电机编号
     * @param speed       步进电机速度
     */
    public String modificationSpeed(int motorNumber, int speed) {
        if (motorNumber <= 0 || motorNumber > StepperMotorConstants.MAX_MOTOR_NO) {
            log.error("编号{}步进电机不存在！", motorNumber);
            return "步进电机编号不存在";
        }
        if (speed >= StepperMotorConstants.MAX_SPEED) {
            log.error("设置速度：{}超过最大速度500了", speed);
            return "设置速度超过最大速度500了";
        }

        String command = buildSpeedCommand(motorNumber, speed);
        log.info("步进电机速度指令：{}", command);
        nettyServerHandler.sendMessageToClient(lanTo485, command, true);
        return "ok";
    }

    private String buildSpeedCommand(int motorNumber, int speed) {
        String motorHex = formatMotorNumber(motorNumber);
        String speedHex = String.format("%04X", speed).toUpperCase();
        String commandWithoutCRC = motorHex + "060005" + speedHex;
        String crc = CRC16.getModbusrtuString(commandWithoutCRC);
        return commandWithoutCRC + crc;
    }

    /**
     * 停止步进电机
     *
     * @param motorNumber 步进电机编号
     */
    public void stop(int motorNumber) {
        if (motorNumber <= 0 || motorNumber > StepperMotorConstants.MAX_MOTOR_NO) {
            log.error("编号{}步进电机不存在！", motorNumber);
            return;
        }

        String command = buildStopCommand(motorNumber);
        log.info("步进电机停机指令：{}", command);
        nettyServerHandler.sendMessageToClient(lanTo485, command, true);
    }

    private String buildStopCommand(int motorNumber) {
        String motorHex = formatMotorNumber(motorNumber);
        String commandWithoutCRC = motorHex + "0600020001";
        String crc = CRC16.getModbusrtuString(commandWithoutCRC);
        return commandWithoutCRC + crc;
    }
}
