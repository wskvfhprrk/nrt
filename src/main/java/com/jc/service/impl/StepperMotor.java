package com.jc.service.impl;

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

    // 定义静态变量来统一管理固定值
    private static final String RESET_COMMAND = "48 3A 01 52 00 00 00 00 00 00 00 00 D5 45 44";
    private static final long SLEEP_TIME_MS = 1000L;
    private static final long POLLING_INTERVAL_MS = 100L;
    private static final int MAX_MOTOR_NO = 3;
    private static final int MAX_SPEED = 500;

    @Autowired
    public StepperMotor(IODeviceHandler ioDeviceHandler) {
        this.ioDeviceHandler = ioDeviceHandler;
    }

    public String bowlRising() {
        return this.startStepperMotor(2, false, 0);
    }

    public String bowlDescent() {
        return this.startStepperMotor(2, true, 0);
    }

    /**
     * 重置碗
     */
    public void bowlReset() {
        // 先重置传感器
        nettyServerHandler.sendMessageToClient(ioIp, RESET_COMMAND, true);
        try {
            // 等待指定时间，确保传感器完成重置
            Thread.sleep(SLEEP_TIME_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 获取传感器状态
        String ioStatus = ioDeviceHandler.getIoStatus();
        if (ioStatus.equals("0")) {
            log.error("传感器没有回信息！");
            return;
        }

        // 解析传感器状态字符串
        String[] split = ioStatus.split(",");
        boolean bowlSensor = split[1].equals("1"); // 碗传感器状态
        boolean lowerLimit = split[2].equals("1"); // 轨道最低极限点状态
        boolean upperLimit = split[3].equals("1"); // 轨道最高极限点状态

        // 如果2、3、4传感器都为低电平，直接升碗
        if (!bowlSensor && !lowerLimit && !upperLimit) {
            this.bowlRising();
            return;
        }

        // 如果传感器2为高电平且传感器3和4为低电平，降碗
        if (bowlSensor && !lowerLimit && !upperLimit) {
            this.bowlDescent();
            // 循环等待传感器2变为低电平
            while (true) {
                String newIoStatus = ioDeviceHandler.getIoStatus();
                if (newIoStatus.split(",")[1].equals("0")) {
                    this.stop(2);
                    break;
                }
                try {
                    Thread.sleep(POLLING_INTERVAL_MS); // 每100毫秒检查一次
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        // 如果到达最高极限位，就降碗
        if (upperLimit) {
            this.bowlDescent();
        }

        // 如果碗传感器为低电平（没有碗）且轨道最高极限点为高电平，提示“已经没有碗，请放碗！”
        if (!bowlSensor && upperLimit) {
            log.error("已经没有碗，请放碗！");
        }

        // 如果碗传感器为高电平（有碗）且轨道最高极限点为高电平，提示“碗放多了，请拿出一部分！”
        if (bowlSensor && upperLimit) {
            log.error("碗放多了，请拿出一部分！");
        }

        // 如果碗传感器为低电平且轨道最低极限点为高电平，升碗
        if (!bowlSensor && lowerLimit) {
            this.bowlRising();
            return;
        }

        // 如果碗传感器为高电平且轨道最高极限点为低电平，降碗
        if (bowlSensor && !upperLimit) {
            this.bowlDescent();
            return;
        }
    }

    /**
     * 启动步进电机
     *
     * @param no                 步进电机编号
     * @param positiveOrNegative 步进电机转动方向，true表示正转，false表示反转
     * @param numberOfPulses     脉冲数量
     */
    public String startStepperMotor(int no, Boolean positiveOrNegative, int numberOfPulses) {
        if (no <= 0 || no > MAX_MOTOR_NO) {
            log.error("编号{}步进电机不存在！", no);
            return "编号" + no + "步进电机不存在"; // 添加return，防止继续执行
        }

        // 检查传感器状态以决定是否可以启动电机
        String[] split = ioDeviceHandler.getIoStatus().split(",");
        if (split[2].equals("1") && no == 2 && positiveOrNegative) {
            log.error("碗已经到最低位，不能再向下走了！");
            return "碗已经到最低位，不能再向下走了！";
        }
        if (split[3].equals("1") && no == 2 && !positiveOrNegative) {
            log.error("碗已经到最高位，不能再向上走了！");
            return "碗已经到最高位，不能再向上走了！";
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
        if (no <= 0 || no > MAX_MOTOR_NO) {
            log.error("编号{}步进电机不存在！", no);
            return "步进电机编号不存在"; // 添加return，防止继续执行
        }
        if (speed >= MAX_SPEED) {
            log.error("设置速度：{}超过最大速度{}了", speed, MAX_SPEED);
            return "设置速度超过最大速度500了";
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
        if (no <= 0 || no > MAX_MOTOR_NO) {
            log.error("编号{}步进电机不存在！", no);
            return; // 添加return，防止继续执行
        }

        String noStr = String.format("%02X", no);
        String command = noStr + "0600020001";
        String crc = CRC16.getModbusrtuString(command);
        command += crc;
        log.info("步进电机停机指令：{}", command);
        nettyServerHandler.sendMessageToClient(lanTo485, command, true);
    }
}
