package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🛠️ [OpenClaw] 尝试使用 channels init 强行激活...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            Map<String, String> envVars = new HashMap<>();
            envVars.put("HOME", baseDir);
            envVars.put("OPENCLAW_GATEWAY_TOKEN", "openclaw_secure_gateway_2026_safe");

            // 1. 启动网关
            ProcessBuilder gatewayPb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            gatewayPb.directory(new File(openclawDir));
            gatewayPb.environment().putAll(envVars);
            gatewayPb.inheritIO();
            Process gatewayProcess = gatewayPb.start();

            // 2. 关键：使用 init 而不是 onboard
            // init 命令会读取 openclaw.json 里的 telegram 配置并强制注入到运行态
            Thread.sleep(8000); 
            System.out.println("📡 正在初始化 Telegram 频道...");
            ProcessBuilder initPb = new ProcessBuilder(nodePath, "dist/index.js", "channels", "init", "telegram");
            initPb.directory(new File(openclawDir));
            initPb.environment().putAll(envVars);
            initPb.inheritIO();
            initPb.start().waitFor();

            // 3. 打印最终状态确认
            new ProcessBuilder(nodePath, "dist/index.js", "status")
                .directory(new File(openclawDir))
                .environment().putAll(envVars)
                .inheritIO()
                .start().waitFor();

            gatewayProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
