package com.jc.controller;

import com.jc.service.impl.TurntableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("turntable")
public class TurntableController {

    @Autowired
    private TurntableService turntableService;

    @GetMapping("reset")
    public String reset(){
       return turntableService.turntableReset();
    }
    @GetMapping("feeding")
    public String feeding(){
       return turntableService.feeding();
    }
}
