package com.jc.constants;

public class StepperMotorConstants {
    /** 重置命令 */
    public static final String RESET_COMMAND = "48 3A 01 52 00 00 00 00 00 00 00 00 D5 45 44";
    /** 睡眠时间（毫秒） */
    public static final long SLEEP_TIME_MS = 1000L;
    /** 最大步进电机编号 */
    public static final int MAX_MOTOR_NO = 4;
    /** 最大速度 */
    public static final int MAX_SPEED = 500;
    /** 碗控制器编号 */
    public static final int BOWL_CONTROLLER_NO = 2;
    /** 转盘控制器编号 */
    public static final int TURNTABLE_CONTROLLER_NO = 3;
    /** 米线控制器编号 */
    public static final int NOODLE_CONTROLLER_NO = 4;
    /** 错误信息：步进电机不存在 */
    public static final String ERROR_NO_MOTOR = "编号%s步进电机不存在！";
    /** 错误信息：碗已到最低位 */
    public static final String ERROR_BOWL_LOWEST = "碗已经到最低位，不能再向下走了！";
    /** 错误信息：碗已到最高位 */
    public static final String ERROR_BOWL_HIGHEST = "碗已经到最高位，不能再向上走了！";
    /** 错误信息：传感器没有响应 */
    public static final String ERROR_SENSOR_NO_RESPONSE = "传感器没有回信息！";
    /** 错误信息：没有碗 */
    public static final String ERROR_NO_BOWL = "已经没有碗，请放碗！";
    /** 错误信息：碗放多了 */
    public static final String ERROR_TOO_MANY_BOWLS = "碗放多了，请拿出一部分！";
    /** 错误信息：速度超过最大速度 */
    public static final String ERROR_SPEED_TOO_HIGH = "设置速度超过最大速度500了";
}
