package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

        try {
            System.out.println("🚀 [Zenix-Cookie-Killer] 正在强制禁用安全校验并启动...");

            // 1. 杀掉旧进程
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Thread.sleep(1000);

            // 2. 启动 N8N (注入禁用安全 Cookie 的关键变量)
            System.out.println("📢 正在解除 N8N 安全 Cookie 限制...");
            ProcessBuilder n8nPb = new ProcessBuilder(baseDir + "/node_modules/.bin/n8n", "start");
            
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196"); 
            n8nEnv.put("N8N_LISTEN_ADDRESS", "0.0.0.0");
            
            // 🚨 重点：这就是解决你那个“不安全 URL/Safari”报错的核心开关
            n8nEnv.put("N8N_SECURE_COOKIE", "false"); 
            
            // 补充优化：1.5G 内存和自检跳过
            n8nEnv.put("NODE_OPTIONS", "--max-old-space-size=1536");
            n8nEnv.put("N8N_SKIP_WEBHOOK_SELF_CHECK", "true");

            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 稳等 10 秒
            Thread.sleep(10000);

            // 4. 启动 OpenClaw
            System.out.println("✅ N8N 安全限制已移除，正在启动 OpenClaw...");
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node", "dist/index.js", "gateway", 
                "--port", "18789", "--token", "mytoken123", "--force"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            cEnv.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1");
            cEnv.put("OPENCLAW_N8N_URL", "http://127.0.0.1:30196/webhook/openclaw");

            clawPb.inheritIO();
            Process pClaw = clawPb.start();

            pClaw.waitFor();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
