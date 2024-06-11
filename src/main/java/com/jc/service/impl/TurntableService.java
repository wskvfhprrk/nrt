//package com.jc.service.impl;
//
//import com.jc.constants.StepperMotorConstants;
//import com.jc.enums.SignalLevel;
//import com.jc.netty.server.NettyServerHandler;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
///**
// * 转台步进电机控制器
// */
//@Service
//@Slf4j
//public class TurntableService {
//    private final NettyServerHandler nettyServerHandler;
//    private final IODeviceService ioDeviceService;
//    private final String lanTo485;
//    @Value("${IoIp}")
//    private String ioIp;
//
//    @Autowired
//    public BowlService(NettyServerHandler nettyServerHandler,
//                       IODeviceService ioDeviceService,
//                       @Value("${lanTo485}") String lanTo485) {
//        this.nettyServerHandler = nettyServerHandler;
//        this.ioDeviceService = ioDeviceService;
//        this.lanTo485 = lanTo485;
//    }
//
//    /**
//     * 碗回原点
//     *
//     * @return
//     */
//    public String turntableReset() {
//        // 获取传感器状态
//        String ioStatus = ioDeviceService.getIoStatus();
//        if (ioStatus.equals(StepperMotorConstants.NOT_INITIALIZED)) {
//            log.error("无法获取传感器的值！");
//            // 先重置传感器
//            nettyServerHandler.sendMessageToClient(ioIp, StepperMotorConstants.RESET_COMMAND, true);
//            try {
//                // 等待指定时间，确保传感器完成重置
//                Thread.sleep(StepperMotorConstants.SLEEP_TIME_MS);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            // 重新获取传感器状态
//            ioStatus = ioDeviceService.getIoStatus();
//        }
//        if(ioStatus.split(",")[0].equals(SignalLevel.HIGH)){
//            log.info("转盘已经在原点位置！");
//            return "ok";
//        }
//        if(ioStatus.split(",")[0].equals(SignalLevel.LOW)){
//            //发送转动转盘指令
//        }
//
//    }
//}
