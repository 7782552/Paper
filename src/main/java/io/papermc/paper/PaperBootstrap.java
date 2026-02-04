package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        // 🚨 爹，如果你的 IP 变了，记得改这里
        String publicIP = "42.119.166.155"; 

        try {
            System.out.println("🚀 [Zenix-Network-Master] 正在强制打通公网连接...");

            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Thread.sleep(1000);

            // 1. 启动 N8N：告诉它你的公网身份
            System.out.println("📢 正在配置 N8N Webhook 地址: http://" + publicIP + ":30196");
            ProcessBuilder n8nPb = new ProcessBuilder(baseDir + "/node_modules/.bin/n8n", "start");
            
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196"); 
            n8nEnv.put("N8N_LISTEN_ADDRESS", "0.0.0.0");
            
            // 🚨 关键：解决机器人不回消息的核心设置
            n8nEnv.put("WEBHOOK_URL", "http://" + publicIP + ":30196/");
            n8nEnv.put("N8N_SECURE_COOKIE", "false"); 
            n8nEnv.put("N8N_SKIP_WEBHOOK_SELF_CHECK", "true");
            // 强制不走隧道，走公网网络
            n8nEnv.put("N8N_TUNNEL_SUBDOMAIN", ""); 

            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            Thread.sleep(12000);

            // 2. 启动 OpenClaw：精准投喂内网 Webhook
            System.out.println("✅ N8N 已就绪，正在激活 OpenClaw 搬运工...");
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node", "dist/index.js", "gateway", 
                "--port", "18789", "--token", "mytoken123", "--force"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            cEnv.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1");
            
            // 🚨 修正：OpenClaw 直接把数据打到本地 Webhook 接口
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
