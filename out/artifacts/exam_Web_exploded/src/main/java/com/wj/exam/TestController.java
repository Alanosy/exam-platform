package com.wj.exam;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/21 16:35
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public String test(){
        return "test";
    }
}
