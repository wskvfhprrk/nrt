package com.jc.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 500称重传感器数据读值解析
 * 读值hex:01 03 9C 40 00 02 EB 8F
 */
@Slf4j
public class AnalysisOfReadingValuesOfWeighingSensorData {
    /**
     * 主方法，程序的入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 从传感器获取的HEX数据字符串
        String hexString = "01 03 04 00 00 06 13 B8 5E";
        // 调用parseHexString方法解析数据
        long decimalValue = parseHexString(hexString);
        // 打印解析后的十进制值
        log.info("解析后的十进制值: {}" ,decimalValue);
        //实际缩小100倍
        log.info("实际重量为：{}kg",decimalValue/100.0);
    }


    /**
     * 解析HEX字符串，提取并转换为十进制值
     * @param hexString 输入的HEX字符串
     * @return 解析后的十进制值
     */
    public static long parseHexString(String hexString) {
        // 通过空格分隔字符串
        String[] hexParts = hexString.split(" ");

        // 确保输入字符串包含正确的部分数量
        if (hexParts.length != 9) {
            throw new IllegalArgumentException("HEX字符串必须包含9个部分。");
        }

        // 提取相关部分: "00 00 06 13"
        String dataPart = hexParts[3] + hexParts[4] + hexParts[5] + hexParts[6];

        // 将HEX字符串转换为十进制值
        long decimalValue = Long.parseLong(dataPart, 16);
        return decimalValue;
    }
}
