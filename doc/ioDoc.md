# DI采集模块指令
>采用智嵌物联32路IO模块，它可同时兼容pnp信号和npn传感器信号
> 
##原始数据采集指令：
**它采用自定义采集协议**
以下是采集到的它的协议
* io个是一个byte四个传感器
* 10表是第一个上电 48 3A 01 41 10 00 00 00 00 00 00 00 C4
* 40表示第二个上电 48 3A 01 41 40 00 00 00 00 00 00 00 C4
* 50表示前两个上电 48 3A 01 41 50 00 00 00 00 00 00 00 C4
* 01表示第三个上电 48 3A 01 41 01 00 00 00 00 00 00 00 C4
* 04表示第四个上电 48 3A 01 41 04 00 00 00 00 00 00 00 C4
* 05表示后两个上电 48 3A 01 41 05 00 00 00 00 00 00 00 C4
* x1高电平：48 3A 01 41 01 00 00 00 00 00 00 00 C4
* x2高电平：48 3A 01 41 04 00 00 00 00 00 00 00 C4
* x1x2高电平：48 3A 01 41 05 00 00 00 00 00 00 00 C4
* x3高电平：48 3A 01 41 10 00 00 00 00 00 00 00 C4
* x4高电平：48 3A 01 41 40 00 00 00 00 00 00 00 C4
* x3x4高电平：48 3A 01 41 50 00 00 00 00 00 00 00 C4 

## 解析方法：
解析方法在`IODeviceHandler`类中