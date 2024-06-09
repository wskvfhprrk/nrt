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
    private IODeviceHandler ioDeviceHandler;

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
     * 启动步进电机
     *
     * @param no                 步进电机编号
     * @param positiveOrNegative 步进电机转动方向，true表示正转，false表示反转
     * @param numberOfPulses     脉冲数量
     */
    public String startStepperMotor(int no, Boolean positiveOrNegative, int numberOfPulses) {
        if (no <= 0 || no > 3) {
            log.error("编号{}步进电机不存在！", no);
            return "编号" + no + "步进电机不存在"; // 添加return，防止继续执行
        }
        //如果继电器3为高电平，严禁电机2向下运行true,如果4为高电平严禁电机3向上运行false
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
        // 将编号转换为16进制字符串
        String noStr = Integer.toHexString(no).toUpperCase();
        // 如果字符串长度不足两位，则在前面补0
        if (noStr.length() < 2) {
            noStr = "0" + noStr;
        }
        // 构造指令字符串
        StringBuffer sb = new StringBuffer(noStr);
        sb.append("060007");
        String pulese = String.format("%04X", numberOfPulses).toUpperCase();
        sb.append(pulese);
        String string = CRC16.getModbusrtuString(sb.toString().replaceAll(" ", ""));
        sb.append(string);
        log.info("脉冲指令：{}", sb);
        // 发送指令
        nettyServerHandler.sendMessageToClient(lanTo485, sb.toString(), true);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 发送正反转
        StringBuffer stringBuffer = new StringBuffer(noStr);
        stringBuffer.append("0600");
        // 正反转指令
        if (positiveOrNegative) {
            stringBuffer.append("00");
        } else {
            stringBuffer.append("01");
        }
        stringBuffer.append("0001");
        String crc = CRC16.getModbusrtuString(stringBuffer.toString().replaceAll(" ", ""));
        stringBuffer.append(crc);
        log.info("步进电机转动指令：{}", stringBuffer);
        nettyServerHandler.sendMessageToClient(lanTo485, stringBuffer.toString(), true);
        return "ok";
    }

    /**
     * 修改步进电机速度
     *
     * @param no    步进电机编号
     * @param speed 步进电机速度
     */
    public String modificationSpeed(int no, int speed) {

        if (no <= 0 || no > 3) {
            log.error("编号{}步进电机不存在！", no);
            return "步进电机编号不存在"; // 添加return，防止继续执行
        }
        if (speed >= 500) {
            log.error("设置速度：{}超过最大速度500了", speed);
            return "设置速度超过最大速度500了";
        }
        // 01 06 00 05 00 01
        String noStr = Integer.toHexString(no).toUpperCase();
        // 如果字符串长度不足两位，则在前面补0
        if (noStr.length() < 2) {
            noStr = "0" + noStr;
        }
        StringBuffer sb = new StringBuffer(noStr);
        // 添加编码
        sb.append("060005");
        String speedStr = Integer.toHexString(speed).toUpperCase();
        speedStr = String.format("%04X", Integer.parseInt(speedStr, 16)).toUpperCase();
        sb.append(speedStr);
        String crc = CRC16.getModbusrtuString(sb.toString().replaceAll(" ", ""));
        sb.append(crc);
        log.info("步进电机速度指令：{}", sb);
        nettyServerHandler.sendMessageToClient(lanTo485, sb.toString(), true);
        return "ok";
    }

    /**
     * 停止步进电机
     *
     * @param no 步进电机编号
     */
    public void stop(int no) {
        if (no <= 0 || no > 3) {
            log.error("编号{}步进电机不存在！", no);
            return; // 添加return，防止继续执行
        }
        // 01 06 00 05 00 01
        String noStr = Integer.toHexString(no).toUpperCase();
        // 如果字符串长度不足两位，则在前面补0
        if (noStr.length() < 2) {
            noStr = "0" + noStr;
        }
        StringBuffer sb = new StringBuffer(noStr);
        // 添加停止
        sb.append("0600020001");
        String crc = CRC16.getModbusrtuString(sb.toString().replaceAll(" ", ""));
        sb.append(crc);
        log.info("步进电机停机指令：{}", sb);
        nettyServerHandler.sendMessageToClient(lanTo485, sb.toString(), true);
    }
}
