package com.jc.constants;

public class Constants {
    /** 重置命令 */
    public static final String RESET_COMMAND = "48 3A 01 52 00 00 00 00 00 00 00 00 D5 45 44";
    /** 睡眠时间（毫秒） */
    public static final long SLEEP_TIME_MS = 100L;
    /** 最大步进电机编号 */
    public static final int MAX_MOTOR_NO = 4;
    /** 最大速度 */
    public static final int MAX_SPEED = 500;
    /** 碗控制器编号 */
    public static final int BOWL_CONTROLLER_NO = 2;
    /** 传感器无值状态为2 */
    public static final String NOT_INITIALIZED = "2";
    /** io传感器在redis存的key值 */
    public static final String IO_KEY = "io";
    /** io传感器在redis存的key值 */
    public static final String IO = "io";
}
