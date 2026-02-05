package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBin = "/home/container/node-v22/bin/node"; 
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";

        try {
            // 这是官方 2026.2.3 要求的强制令牌，不要改动，除非你在 n8n 里同步修改
            String myFixedToken = "admin123"; 

            System.out.println("🦞 [System-Fusion] 启动双引擎模式...");
            System.out.println("🔗 n8n 入口: https://8.8855.cc.cd/");
            System.out.println("🧠 OpenClaw 网关: 127.0.0.1:18789 (Token: " + myFixedToken + ")");

            // --- 1. 启动 n8n ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196");
            n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();

            // --- 2. 启动 OpenClaw ---
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", "--allow-unconfigured", "--port", "18789"
            );
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            
            // 官方环境变量注入
            ocEnv.put("OPENCLAW_GATEWAY_TOKEN", myFixedToken);
            ocEnv.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0");
            ocEnv.put("OPENCLAW_AI_PROVIDER", "google");
            ocEnv.put("OPENCLAW_AI_MODEL", "gemini-1.5-pro-latest");
            ocEnv.put("OPENCLAW_AI_GOOGLE_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            
            // 确保 OpenClaw 不去抢 Telegram 句柄
            ocEnv.put("OPENCLAW_TELEGRAM_ENABLED", "false"); 

            ocPb.inheritIO().start();
            System.out.println("🚀 混合架构已就绪。请前往 n8n 配置 HTTP 请求至 18789 端口。");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
