package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String myDomain = "8.8855.cc.cd"; 

        try {
            System.out.println("🚀 [Zenix-Cloudflare-Pro] 正在切换至 HTTPS 云端模式...");

            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Thread.sleep(1000);

            // 1. 启动 N8N (注意这里 Webhook URL 变成了 https，且没有端口尾巴)
            ProcessBuilder n8nPb = new ProcessBuilder(baseDir + "/node_modules/.bin/n8n", "start");
            
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196"); 
            n8nEnv.put("N8N_LISTEN_ADDRESS", "0.0.0.0");
            
            // 🚨 核心修改：既然用了 CF Proxy，这里必须用 https，且不用写 :30196
            n8nEnv.put("WEBHOOK_URL", "https://" + myDomain + "/");
            n8nEnv.put("N8N_SECURE_COOKIE", "false"); 
            n8nEnv.put("N8N_SKIP_WEBHOOK_SELF_CHECK", "true");

            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            Thread.sleep(10000);

            // 2. 启动 OpenClaw
            System.out.println("✅ N8N 已就绪，同步启动 OpenClaw...");
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

            // 3. 自动审批
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
