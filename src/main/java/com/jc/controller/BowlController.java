/**
 * 控制器，负责接收碗相关请求并调用相应服务实现类完成操作
 */
package com.jc.controller;

import com.jc.service.impl.BowlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("bowl")
public class BowlController {

    @Autowired
    private BowlService bowlService;

    /**
     * 连续出碗检查
     *
     * @return 操作结果
     */
    @GetMapping("continuousBowlCheck")
    public String continuousBowlCheck() {
        bowlService.continuousBowlCheck();
        return "ok";
    }

    /**
     * 碗上升
     *
     * @return 操作结果
     */
    @GetMapping("bowlRising")
    public String bowlRising() {
        return bowlService.bowlRising();
    }

    /**
     * 碗下降
     *
     * @return 操作结果
     */
    @GetMapping("bowlDescent")
    public String bowlDescent() {
        return bowlService.bowlDescent();
    }

    /**
     * 碗重置
     *
     * @return 操作结果
     */
    @GetMapping("bowlReset")
    public String bowlReset() {
        new Thread(() -> {
            bowlService.bowlReset();
        }).start();
        return "ok";
    }
}
