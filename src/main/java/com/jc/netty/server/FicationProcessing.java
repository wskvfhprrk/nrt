package com.jc.netty.server;

import com.jc.service.impl.IODeviceHandler;
import com.jc.service.impl.RelayDeviceHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 分设备处理
 */
@Service
@Slf4j
public class FicationProcessing {
    @Value("${IoIp}")
    private String ioIp;
    @Autowired
    private IODeviceHandler ioDeviceHandler;
    @Value("${relayIp}")
    private String relayIp;
    @Autowired
    @Lazy
    private RelayDeviceHandler relayDeviceHandler;
    @Value("${lanTo485}")
    private String lanTo485;
    /**
     * 分类处理
     *
     * @param clientIp
     * @param flag
     * @param message
     */
    public void classificationProcessing(String clientIp, boolean flag, String message) {
        if (clientIp.equals(ioIp)) {
            ioDeviceHandler.handle(message, flag);
        } else if(clientIp.equals(relayIp)) {
            relayDeviceHandler.handle(message, flag);
        }else {
            log.error("未知的设备ip");
        }
    }
}
