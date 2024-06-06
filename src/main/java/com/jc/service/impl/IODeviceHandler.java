package com.jc.service.impl;

import com.jc.service.DeviceHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IODeviceHandler implements DeviceHandler {
    @Override
    public void handle(String message, boolean isHex) {
        if (isHex) {
//            log.info(" HEX消息: {}", message);
            //查中间8位，从第6位开始查询
            String[] split = message.split(" ");
            //split[4]——X1-X4
            // 将字符串分割为8个部分，每个部分4个字符
            StringBuffer sb=new StringBuffer();
            for (int i = 1; i <= 32; i++) {
                if (i % 4 == 0) {
//                    log.info("X{}-X{}位数为：{}", i-3, i, split[3+i/4]);
                    //解析高低电平
                    heightOrLow(i-3,i,split[3+i/4],sb);
                }
            }
            log.info("传感器的高低电平：{}",sb);
        } else {
            log.info("普通消息: {}", message);
            // 在这里添加处理私有IP地址的普通字符串的逻辑
        }
    }

    /**
     * 解析x脚的高低电平
     * @param StartIo 启始引脚
     * @param endIo
     * @param hexStr
     */
    private StringBuffer heightOrLow(int StartIo, int endIo, String hexStr,StringBuffer sb) {
        // 检查 hexStr 是否为空或长度是否小于2
        if (hexStr == null || hexStr.length() < 2) {
            log.error("无效的 hexStr 输入");
            return null;
        }

        // 提取 hexStr 的第一位和第二位字符
        char firstChar = hexStr.charAt(0);
        char secondChar = hexStr.charAt(1);

        // 根据第二位字符判断 StartIo 是否为高电平HHH
        if (secondChar == '1' || secondChar == '5') {
            sb.append("1,");
            log.info("引脚 {} 为高电平HHH", StartIo);
        } else {
            sb.append("0,");
            log.info("引脚 {} 为低电平", StartIo);
        }

        // 根据第二位字符判断 StartIo+1 是否为高电平HHH
        if (secondChar == '4' || secondChar == '5') {
            sb.append("1,");
            log.info("引脚 {} 为高电平HHH", StartIo + 1);
        } else {
            sb.append("0,");
            log.info("引脚 {} 为低电平", StartIo + 1);
        }

        // 根据第一位字符判断 StartIo+2 是否为高电平HHH
        if (firstChar == '1' || firstChar == '5') {
            sb.append("1,");
            log.info("引脚 {} 为高电平HHH", StartIo + 2);
        } else {
            sb.append("0,");
            log.info("引脚 {} 为低电平", StartIo + 2);
        }

        // 根据第一位字符判断 endIo 是否为高电平HHH
        if (firstChar == '4' || firstChar == '5') {
            sb.append("1,");
            log.info("引脚 {} 为高电平HHH", endIo);
        } else {
            sb.append("0,");
            log.info("引脚 {} 为低电平", endIo);
        }
        return sb;
    }
}
