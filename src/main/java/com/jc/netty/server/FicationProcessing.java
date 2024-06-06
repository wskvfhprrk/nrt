package com.jc.netty.server;

import com.jc.service.impl.IODeviceHandler;
import com.jc.service.impl.RelayDeviceHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 分类处理服务
 * 用于根据客户端IP地址将消息分类处理到对应的设备处理器
 */
@Service
@Slf4j
public class FicationProcessing {

    @Value("${IoIp}")
    private String ioIp; // IO设备IP地址
    @Autowired
    private IODeviceHandler ioDeviceHandler; // IO设备处理器
    @Value("${relayIp}")
    private String relayIp; // 继电器设备IP地址
    @Autowired
    @Lazy
    private RelayDeviceHandler relayDeviceHandler; // 继电器设备处理器
    @Value("${lanTo485}")
    private String lanTo485; // 485设备IP地址（暂未使用）

    /**
     * 分类处理方法
     *
     * @param clientIp 客户端IP地址
     * @param flag     是否为16进制消息
     * @param message  消息内容
     */
    public void classificationProcessing(String clientIp, boolean flag, String message) {
        // 根据客户端IP地址分类处理消息到对应的设备处理器
        if (clientIp.equals(ioIp)) {
            // 如果客户端IP地址匹配IO设备IP地址，则交由IO设备处理器处理消息
            ioDeviceHandler.handle(message, flag);
        } else if (clientIp.equals(relayIp)) {
            // 如果客户端IP地址匹配继电器设备IP地址，则交由继电器设备处理器处理消息
            relayDeviceHandler.handle(message, flag);
        } else {
            // 其他情况视为未知设备IP地址，记录错误日志
            log.error("未知的设备IP地址：{}", clientIp);
        }
    }
}
