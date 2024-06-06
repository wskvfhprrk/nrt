package com.jc.service;

/**
 * 设备处理器接口
 * 定义了处理设备消息的基本方法
 */
public interface DeviceHandler {

    /**
     * 处理消息
     *
     * @param message 消息内容
     * @param isHex 是否为16进制消息
     */
    void handle(String message, boolean isHex);
}
