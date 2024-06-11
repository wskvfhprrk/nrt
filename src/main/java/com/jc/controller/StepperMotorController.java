/**
 * 控制器，负责接收步进电机相关请求并调用相应服务实现类完成操作
 */
package com.jc.controller;

import com.jc.service.impl.StepperMotorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("stepperMotor")
public class StepperMotorController {

    @Autowired
    private StepperMotorService stepperMotorService;

    /**
     * 启动步进电机
     *
     * @param no                 步进电机编号
     * @param positiveOrNegative 步进电机转动方向，true表示正转，false表示反转
     * @param numberOfPulses     脉冲数量
     * @return 返回操作结果
     */
    @GetMapping("startStepperMotor")
    public String startStepperMotor(int no, Boolean positiveOrNegative, int numberOfPulses) {
        return stepperMotorService.startStepperMotor(no, positiveOrNegative, numberOfPulses);
    }

    /**
     * 修改步进电机速度
     *
     * @param no    步进电机编号
     * @param speed 步进电机速度
     * @return 返回操作结果
     */
    @GetMapping("modificationSpeed")
    public String modificationSpeed(int no, int speed) {
        return stepperMotorService.modificationSpeed(no, speed);
    }

    /**
     * 停止步进电机
     *
     * @param no 步进电机编号
     * @return 返回操作结果
     */
    @GetMapping("stop")
    public String stop(int no) {
        stepperMotorService.stop(no);
        return "ok";
    }
}
