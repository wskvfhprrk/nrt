package com.jc.utils;

/**
 * Created by hejz on 2018/4/17.
 */
public class HexConvert {

public static String  convertStringToHex(String str){

    char[] chars = str.toCharArray();

    StringBuffer hex = new StringBuffer();
    for(int i = 0; i < chars.length; i++){
        hex.append(Integer.toHexString((int)chars[i]));
    }

    return hex.toString();
}

    /**
     * 16进制字符串串字符串
     * @param hex
     * @return
     */
    public static String convertHexToString(String hex){

    StringBuilder sb = new StringBuilder();
    StringBuilder sb2 = new StringBuilder();

    for( int i=0; i<hex.length()-1; i+=2 ){

        String s = hex.substring(i, (i + 2));           
        int decimal = Integer.parseInt(s, 16);          
        sb.append((char)decimal);
        sb2.append(decimal);
    }

    return sb.toString();
}

    /**
     * 16进制字符串转bytes
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
    if (hexString == null || hexString.equals("")) {
        return null;
    }
    // toUpperCase将字符串中的所有字符转换为大写
    hexString = hexString.toUpperCase();
    int length = hexString.length() / 2;
    // toCharArray将此字符串转换为一个新的字符数组。
    char[] hexChars = hexString.toCharArray();
    byte[] d = new byte[length];
    for (int i = 0; i < length; i++) {
        int pos = i * 2;
        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
    }
    return d;
}

    /**
     * 返回匹配字符
     * @param c
     * @return
     */
    private static byte charToByte(char c) {
    return (byte) "0123456789ABCDEF".indexOf(c);
}

    /**
     * 将字节数组转换为short类型，即统计字符串长度
     * @param b
     * @return
     */
    public static short bytes2Short2(byte[] b) {
    short i = (short) (((b[1] & 0xff) << 8) | b[0] & 0xff);
    return i;
}

    /**
     * 将字节数组转换为16进制字符串
     * @param bytes
     * @return
     */
    public static String BinaryToHexString(byte[] bytes) {
    String hexStr = "0123456789ABCDEF";
    String result = "";
    String hex = "";
    for (byte b : bytes) {
        hex = String.valueOf(hexStr.charAt((b & 0xF0) >> 4));
        hex += String.valueOf(hexStr.charAt(b & 0x0F));
        result += hex + " ";
    }
    return result;
}

    /**
     * 16进制字符串转字符串
     * @param hexStr
     * @return
     */
    public static String hexStringToString(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

public static void main(String[] args) {
    System.out.println("======ASCII码转换为16进制======");
    String str = HexConvert.convertHexToString("0000");
    System.out.println("字符串: " + str);
    String hex = HexConvert.convertStringToHex(str);
    System.out.println("====转换为16进制=====" + hex);

    System.out.println("======16进制转换为ASCII======");
    System.out.println("Hex : " + hex);
    System.out.println("ASCII : " + HexConvert.convertHexToString(hex));

    byte[] bytes = HexConvert.hexStringToBytes( hex );
    System.out.println(HexConvert.BinaryToHexString( bytes ));
}
}