package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🛸 [OpenClaw] 配置已就绪，正在尝试联合启动模式...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            // 1. 设置通用环境变量
            Map<String, String> envVars = new HashMap<>();
            envVars.put("HOME", baseDir);
            envVars.put("OPENCLAW_GATEWAY_TOKEN", "123456789");

            // 2. 启动网关 (这次我们换一种方式，先让它在后台跑起来)
            System.out.println("🛰️ 正在启动网关核心...");
            ProcessBuilder gatewayPb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            gatewayPb.directory(new File(openclawDir));
            gatewayPb.environment().putAll(envVars);
            gatewayPb.inheritIO();
            Process gatewayProcess = gatewayPb.start();

            // ⚠️ 关键步骤：等待网关稳定后，发送“激活 Telegram”指令
            Thread.sleep(5000); 
            System.out.println("📡 正在向网关发送 Telegram 激活指令...");
            
            // 使用 message 指令强行触发频道初始化 (参考 LilysAI 指南中提到的 message 模块)
            ProcessBuilder activatePb = new ProcessBuilder(nodePath, "dist/index.js", "channels", "connect", "telegram");
            activatePb.directory(new File(openclawDir));
            activatePb.environment().putAll(envVars);
            activatePb.inheritIO();
            activatePb.start().waitFor();

            // 保持主进程运行
            gatewayProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
