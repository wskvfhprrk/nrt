package com.jc.service.impl;

import com.jc.constants.StepperMotorConstants;
import com.jc.netty.server.NettyServerHandler;
import com.jc.utils.CRC16;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 步进电机服务实现类，负责控制步进电机的操作，如启动、停止和速度调整
 */
@Service
@Slf4j
public class StepperMotorService {

    private final NettyServerHandler nettyServerHandler;
    private final String lanTo485;

    @Value("${IoIp}")
    private String ioIp;

    public StepperMotorService(NettyServerHandler nettyServerHandler, @Value("${lanTo485}") String lanTo485) {
        this.nettyServerHandler = nettyServerHandler;
        this.lanTo485 = lanTo485;
    }

    /**
     * 启动步进电机
     *
     * @param motorNumber        步进电机编号
     * @param positiveOrNegative 步进电机转动方向，true表示正转，false表示反转
     * @param numberOfPulses     脉冲数量
     * @return 操作结果
     */
    public String startStepperMotor(int motorNumber, boolean positiveOrNegative, int numberOfPulses) {
        if (motorNumber <= 0 || motorNumber > StepperMotorConstants.MAX_MOTOR_NO) {
            log.error("编号{}步进电机不存在！", motorNumber);
            return "编号" + motorNumber + "步进电机不存在";
        }

        sendPulseCommand(motorNumber, numberOfPulses);
        sendRotationCommand(motorNumber, positiveOrNegative);
        return "操作成功";
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

    /**
     * 修改步进电机速度
     *
     * @param motorNumber 步进电机编号
     * @param speed       步进电机速度
     * @return 操作结果
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
        return "操作成功";
    }

    /**
     * 发送脉冲指令
     *
     * @param motorNumber    步进电机编号
     * @param numberOfPulses 脉冲数量
     */
    private void sendPulseCommand(int motorNumber, int numberOfPulses) {
        String command = buildPulseCommand(motorNumber, numberOfPulses);
        log.info("脉冲指令：{}", command);
        nettyServerHandler.sendMessageToClient(lanTo485, command, true);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            log.error("执行脉冲指令时发生InterruptedException", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 构建脉冲指令
     *
     * @param motorNumber    步进电机编号
     * @param numberOfPulses 脉冲数量
     * @return 脉冲指令
     */
    private String buildPulseCommand(int motorNumber, int numberOfPulses) {
        String motorHex = formatMotorNumber(motorNumber);
        String pulseHex = String.format("%04X", numberOfPulses).toUpperCase();
        String commandWithoutCRC = motorHex + "060007" + pulseHex;
        String crc = CRC16.getModbusrtuString(commandWithoutCRC);
        return commandWithoutCRC + crc;
    }

    /**
     * 发送转动指令
     *
     * @param motorNumber        步进电机编号
     * @param positiveOrNegative 转动方向
     */
    private void sendRotationCommand(int motorNumber, boolean positiveOrNegative) {
        String command = buildRotationCommand(motorNumber, positiveOrNegative);
        log.info("步进电机转动指令：{}", command);
        nettyServerHandler.sendMessageToClient(lanTo485, command, true);
    }

    /**
     * 构建转动指令
     *
     * @param motorNumber        步进电机编号
     * @param positiveOrNegative 转动方向
     * @return 转动指令
     */
    private String buildRotationCommand(int motorNumber, boolean positiveOrNegative) {
        String motorHex = formatMotorNumber(motorNumber);
        String directionHex = positiveOrNegative ? "00" : "01";
        String commandWithoutCRC = motorHex + "0600" + directionHex + "0001";
        String crc = CRC16.getModbusrtuString(commandWithoutCRC);
        return commandWithoutCRC + crc;
    }

    /**
     * 构建速度指令
     *
     * @param motorNumber 步进电机编号
     * @param speed       速度
     * @return 速度指令
     */
    private String buildSpeedCommand(int motorNumber, int speed) {
        String motorHex = formatMotorNumber(motorNumber);
        String speedHex = String.format("%04X", speed).toUpperCase();
        String commandWithoutCRC = motorHex + "060005" + speedHex;
        String crc = CRC16.getModbusrtuString(commandWithoutCRC);
        return commandWithoutCRC + crc;
    }

    /**
     * 格式化步进电机编号
     *
     * @param motorNumber 步进电机编号
     * @return 格式化后的步进电机编号
     */
    private String formatMotorNumber(int motorNumber) {
        return String.format("%02X", motorNumber).toUpperCase();
    }

    /**
     * 构建停止指令
     *
     * @param motorNumber 步进电机编号
     * @return 停止指令
     */
    private String buildStopCommand(int motorNumber) {
        String motorHex = formatMotorNumber(motorNumber);
        String commandWithoutCRC = motorHex + "0600020001";
        String crc = CRC16.getModbusrtuString(commandWithoutCRC);
        return commandWithoutCRC + crc;
    }
}
