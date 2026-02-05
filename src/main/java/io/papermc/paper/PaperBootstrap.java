package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBin = "/home/container/node-v22/bin/node"; 
        String ocBin = baseDir + "/node_modules/.bin/openclaw";
        String ocStateDir = baseDir + "/.openclaw";

        try {
            System.out.println("🦞 [System-Fusion] 正在初始化 2026 安全网关...");

            // --- 0. 预设安全令牌 (你可以把 admin123 改成你想要的) ---
            String myGatewayToken = "admin123"; 

            // --- 1. 环境准备 ---
            File stateDir = new File(ocStateDir);
            if (!stateDir.exists()) stateDir.mkdirs();
            Files.deleteIfExists(Paths.get(ocStateDir, "openclaw.json"));
            Files.write(Paths.get(ocStateDir, ".onboarded"), "true".getBytes());

            // --- 2. 启动 OpenClaw ---
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", 
                "--allow-unconfigured",
                "--port", "18789"
            );
            
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            ocEnv.put("OPENCLAW_STATE_DIR", ocStateDir);
            
            // --- 核心配置：令牌注入 ---
            // 对应文档中的 gateway.auth.token
            ocEnv.put("OPENCLAW_GATEWAY_TOKEN", myGatewayToken); 
            ocEnv.put("OPENCLAW_GATEWAY_AUTH", "token"); // 显式声明使用令牌模式
            ocEnv.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0");

            // Telegram 配置
            ocEnv.put("OPENCLAW_TELEGRAM_ENABLED", "true");
            ocEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            
            // AI 配置
            ocEnv.put("OPENCLAW_AI_PROVIDER", "google");
            ocEnv.put("OPENCLAW_AI_GOOGLE_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");

            ocPb.inheritIO().start();
            System.out.println("🚀 网关已启动！");
            System.out.println("🔑 你的网关连接令牌为: " + myGatewayToken);
            System.out.println("🌐 仪表盘地址: http://你的服务器IP:18789");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
