package com.jc.service.impl;

import com.jc.constants.StepperMotorConstants;
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

    @Autowired
    public StepperMotorService(NettyServerHandler nettyServerHandler,
                               IODeviceService ioDeviceService,
                               @Value("${lanTo485}") String lanTo485) {
        this.nettyServerHandler = nettyServerHandler;
        this.ioDeviceService = ioDeviceService;
        this.lanTo485 = lanTo485;
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
        return (ioStatus[2].equals("1") && motorNumber == 2 && positiveOrNegative) ||
               (ioStatus[3].equals("1") && motorNumber == 2 && !positiveOrNegative);
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
