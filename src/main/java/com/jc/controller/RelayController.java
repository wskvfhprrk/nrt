package com.jc.controller;

import com.jc.service.impl.RelayDeviceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RelayController {

    @Autowired
    private RelayDeviceHandler relayDeviceHandler;

    @GetMapping("closeAll")
    public void closeAll(){
        relayDeviceHandler.closeAll();
    }
    @GetMapping("open")
    public void open(int i){
        relayDeviceHandler.relayOpening(i);
    }
    @GetMapping("close")
    public void close(int i){
        relayDeviceHandler.relayClosing(i);
    }
    @GetMapping("openAll")
    public void openAll(){
        relayDeviceHandler.openAll();
    }
    @GetMapping("openClose")
    public void openClose(int no,int second){
        relayDeviceHandler.openClose(no,second);
    }
}
