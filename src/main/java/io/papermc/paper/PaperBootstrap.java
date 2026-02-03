package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🛠️ [OpenClaw] 正在执行 2026 版安全合规修复与强行拉起...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            // 1. 物理修复安全审计中提到的权限问题 
            System.out.println("🔐 正在强制锁定凭据目录 (chmod 700)...");
            new ProcessBuilder("chmod", "-R", "700", baseDir + "/.openclaw").start().waitFor();

            Map<String, String> envVars = new HashMap<>();
            envVars.put("HOME", baseDir);
            // 建议使用更长的 Token 规避警告 
            envVars.put("OPENCLAW_GATEWAY_TOKEN", "openclaw_secure_gateway_2026_safe");

            // 2. 启动核心网关 
            System.out.println("🛰️ 正在点火网关服务器...");
            ProcessBuilder gatewayPb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            gatewayPb.directory(new File(openclawDir));
            gatewayPb.environment().putAll(envVars);
            gatewayPb.inheritIO();
            Process gatewayProcess = gatewayPb.start();

            // 3. 强制唤醒频道 (使用 onboard 修复模式替代 connect)
            Thread.sleep(10000); // 增加等待时间确保 PID 51 完全稳定 
            System.out.println("📡 正在执行频道强行加载...");
            ProcessBuilder onboardPb = new ProcessBuilder(nodePath, "dist/index.js", "onboard", "--confirm-all");
            onboardPb.directory(new File(openclawDir));
            onboardPb.environment().putAll(envVars);
            onboardPb.inheritIO();
            onboardPb.start().waitFor();

            gatewayProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
