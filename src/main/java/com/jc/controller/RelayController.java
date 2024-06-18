package com.jc.controller;

import com.jc.service.impl.RelayDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 继电器控制器
 */
@RestController
public class RelayController {

    @Autowired
    private RelayDeviceService relayDeviceService;

    /**
     * 关闭所有继电器
     */
    @GetMapping("closeAll")
    public void closeAll() {
        relayDeviceService.closeAll();
    }

    /**
     * 打开指定编号的继电器
     *
     * @param i 继电器编号
     */
    @GetMapping("open")
    public void open(@RequestParam int i) {
        relayDeviceService.relayOpening(i);
    }

    /**
     * 关闭指定编号的继电器
     *
     * @param i 继电器编号
     */
    @GetMapping("close")
    public void close(@RequestParam int i) {
        relayDeviceService.relayClosing(i);
    }

    /**
     * 打开所有继电器
     */
    @GetMapping("openAll")
    public void openAll() {
        relayDeviceService.openAll();
    }
    @GetMapping("dispenseSoup")
    public void dispenseSoup() {
        relayDeviceService.dispenseSoup();
    }

    /**
     * 打开指定编号的继电器一段时间后自动关闭
     *
     * @param no     继电器编号
     * @param second 自动关闭的时间（秒）
     */
    @GetMapping("openClose")
    public void openClose(@RequestParam int no, @RequestParam int second) {
        relayDeviceService.openClose(no, second);
    }
}
