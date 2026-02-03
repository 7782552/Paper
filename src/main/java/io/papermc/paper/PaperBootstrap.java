package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🛠️ [OpenClaw] 正在修复目录权限并强行激活频道...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            Map<String, String> envVars = new HashMap<>();
            envVars.put("HOME", baseDir);
            envVars.put("OPENCLAW_GATEWAY_TOKEN", "123456789");

            // 1. 修复审计警告中的目录权限 (关键：满足安全策略)
            System.out.println("🔐 修复凭据目录权限...");
            new ProcessBuilder("chmod", "700", baseDir + "/.openclaw/credentials").start().waitFor();

            // 2. 启动网关核心
            ProcessBuilder gatewayPb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            gatewayPb.directory(new File(openclawDir));
            gatewayPb.environment().putAll(envVars);
            gatewayPb.inheritIO();
            Process gatewayProcess = gatewayPb.start();

            Thread.sleep(8000); 

            // 3. 强行深度探测频道 (这会迫使系统加载 Telegram 插件)
            System.out.println("🔍 执行深度探测激活频道...");
            ProcessBuilder deepPb = new ProcessBuilder(nodePath, "dist/index.js", "status", "--deep");
            deepPb.directory(new File(openclawDir));
            deepPb.environment().putAll(envVars);
            deepPb.inheritIO();
            deepPb.start().waitFor();

            gatewayProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
