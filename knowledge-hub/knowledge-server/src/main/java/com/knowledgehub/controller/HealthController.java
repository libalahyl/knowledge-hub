package com.knowledgehub.controller;

import com.knowledgehub.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查
 */
@RestController
public class HealthController {

    @GetMapping("/api/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
