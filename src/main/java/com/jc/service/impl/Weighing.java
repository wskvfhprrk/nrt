package com.jc.service.impl;

import com.jc.service.DeviceHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class Weighing implements DeviceHandler {

    @Value("${lanTo485}")
    private String lanTo485;

    @Override
    public void handle(String message, boolean isHex) {
        if (isHex) {
            log.info("HEX消息: {}", message);
        } else {
            log.info("普通消息: {}", message);
            // 在这里添加处理普通字符串消息的逻辑
        }
    }
}
