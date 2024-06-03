package com.jc.controller;

import com.jc.entity.Order;
import com.jc.netty.NettyServerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:8080") // 允许来自该域的请求
public class OrderController {

    @Autowired
    private NettyServerHandler nettyServerHandler;
    @Value("${lan_to_485}")
    private String lan485;

    @PostMapping
    public ResponseEntity<String> submitOrder(@RequestBody Order order) {
        // 在这里处理订单逻辑，例如保存到数据库或其他操作
        log.info("收到订单: " + order);
        nettyServerHandler.sendMessageToClient(lan485,order.toString());
        return new ResponseEntity<>("订单提交成功", HttpStatus.OK);
    }
}
