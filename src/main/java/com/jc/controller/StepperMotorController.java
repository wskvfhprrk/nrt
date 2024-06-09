/**
 * 控制器，负责接收步进电机相关请求并调用相应服务实现类完成操作
 */
package com.jc.controller;

import com.jc.service.impl.StepperMotor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("stepperMotor")
public class StepperMotorController {

    @Autowired
    private StepperMotor stepperMotor;

    /**
     * 启动步进电机
     *
     * @param no               步进电机编号
     * @param positiveOrNegative   步进电机转动方向，true表示正转，false表示反转
     * @param numberOfPulses   脉冲数量
     * @return 返回操作结果
     */
    @GetMapping("startStepperMotor")
    public String startStepperMotor(int no, Boolean positiveOrNegative, int numberOfPulses){
        return stepperMotor.startStepperMotor(no,positiveOrNegative,numberOfPulses);
    }

    /**
     * 修改步进电机速度
     *
     * @param no     步进电机编号
     * @param speed  步进电机速度
     * @return 返回操作结果
     */
    @GetMapping("modificationSpeed")
    public String modificationSpeed(int no, int speed){
        return stepperMotor.modificationSpeed(no,speed);
    }

    /**
     * 停止步进电机
     *
     * @param no   步进电机编号
     * @return 返回操作结果
     */
    @GetMapping("stop")
    public String stop(int no){
        stepperMotor.stop(no);
        return "ok";
    }
}
