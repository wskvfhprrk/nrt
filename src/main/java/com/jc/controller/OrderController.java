package com.jc.controller;

import com.jc.entity.Order;
import com.jc.netty.server.NettyServerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:8080") // 允许来自该域的请求
public class OrderController {

    @Autowired
    private NettyServerHandler nettyServerHandler;

    @Value("${lanTo485}")
    private String lanTo485;

    /**
     * 提交订单
     *
     * @param order 订单对象
     * @return ResponseEntity<String> 响应实体
     */
    @PostMapping
    public ResponseEntity<String> submitOrder(@RequestBody Order order) {
        // 在这里处理订单逻辑，例如保存到数据库或其他操作
        log.info("收到订单: " + order);
        // 发送订单信息到设备
        nettyServerHandler.sendMessageToClient(lanTo485, order.toString(), false);
        return new ResponseEntity<>("订单提交成功", HttpStatus.OK);
    }
}
