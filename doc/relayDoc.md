# 继电器采集模块

>采用智嵌物联32路继电器
> 
## 采集到的协议

### 标准的modbus协议：

**不使用**

### 智嵌自定义协议：

#### 打开第一个继电器：

TX：48 3A 01 70 01 01 00 00 45 44  时间: 17:39:01

RX：48 3A 01 71 01 01 00 00 45 44  时间: 17:39:01
#### 关闭第一个继电器：

TX：48 3A 01 70 01 00 00 00 45 44  时间: 17:39:08

RX：48 3A 01 71 01 00 00 00 45 44  时间: 17:39:08

**其中48 3A 01 70后01表示第几个继电器，后面01表示打开，00表示关闭**

#### 全部打开：

TX：48 3A 01 57 55 55 55 55 55 55 55 55 82 45 44  时间: 17:41:38

RX：48 3A 01 54 55 55 55 55 55 55 55 55 7F 45 44  时间: 17:41:38
#### 全部关闭：

TX：48 3A 01 57 00 00 00 00 00 00 00 00 DA 45 44  时间: 17:42:19

RX：48 3A 01 54 00 00 00 00 00 00 00 00 D7 45 44  时间: 17:42:19

## 代码
解析类：`RelayDeviceHandler`