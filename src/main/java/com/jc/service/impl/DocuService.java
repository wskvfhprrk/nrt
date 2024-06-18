package com.jc.service.impl;

import com.jc.service.DeviceHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocuService implements DeviceHandler {
    /**
     * 处理消息
     *
     * @param message 消息内容
     * @param isHex   是否为16进制消息
     */
    @Override
    public void handle(String message, boolean isHex) {
        if (isHex) {
            log.info("HEX消息: {}", message);
        } else {
            log.info("普通消息: {}", message);
        }
    }
}
