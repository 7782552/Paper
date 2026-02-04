package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

        try {
            System.out.println("🚀 [Zenix-Gold-Edition] 1.5G 内存优化方案启动...");

            // 1. 强制清理残留
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Thread.sleep(1000);

            // 2. 启动 N8N：锁定 1536MB 内存
            System.out.println("📢 正在为 N8N 注入 1.5G 运行动力...");
            ProcessBuilder n8nPb = new ProcessBuilder(baseDir + "/node_modules/.bin/n8n", "start");
            
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196"); 
            n8nEnv.put("N8N_LISTEN_ADDRESS", "0.0.0.0");
            
            // 🚨 核心修改：分配 1.5G 内存 (1024 * 1.5 = 1536)
            n8nEnv.put("NODE_OPTIONS", "--max-old-space-size=1536");
            
            // 🚨 稳定性优化
            n8nEnv.put("N8N_SKIP_WEBHOOK_SELF_CHECK", "true");
            n8nEnv.put("N8N_METRICS", "false"); 

            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 缓冲 15 秒，让内存分配完成
            System.out.println("⏳ N8N 正在热身，预计 15 秒后开启 OpenClaw...");
            Thread.sleep(15000);

            // 4. 启动 OpenClaw
            System.out.println("✅ N8N 已就绪，正在拉起 OpenClaw 助理...");
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node",
                "dist/index.js", "gateway", 
                "--port", "18789", 
                "--token", "mytoken123",
                "--force"
            );
            
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            cEnv.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1");
            cEnv.put("OPENCLAW_N8N_URL", "http://127.0.0.1:30196/webhook/openclaw");

            clawPb.inheritIO();
            Process pClaw = clawPb.start();

            // 5. 自动审批
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(pClaw.getOutputStream()));
            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(10000);
                        writer.write("pairing approve telegram all\n");
                        writer.flush();
                    }
                } catch (Exception ignored) {}
            }).start();

            pClaw.waitFor();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
