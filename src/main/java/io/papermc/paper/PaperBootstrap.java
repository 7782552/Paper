package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin"; // Node 的家
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

        try {
            System.out.println("🚀 [Zenix-Ultra-Final] 正在注入环境变量并启动全系统...");

            // 1. 彻底清理
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 启动 N8N (注入 Node 路径)
            System.out.println("🔥 正在精准拉起 N8N...");
            // 咱们已经知道 N8N 在这个路径：/home/container/node_modules/.bin/n8n
            ProcessBuilder n8nPb = new ProcessBuilder(baseDir + "/node_modules/.bin/n8n", "start", "--port", "30196");
            
            Map<String, String> n8nEnv = n8nPb.environment();
            // 🚨 关键修复：把我们的 node 路径加到系统的 PATH 里
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("NODE_PATH", baseDir + "/node_modules");
            
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 等待 N8N 启动
            Thread.sleep(5000);

            // 4. 启动 OpenClaw (内网模式)
            System.out.println("✅ 正在拉起 OpenClaw 后台组件...");
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node",
                "dist/index.js", "gateway", 
                "--port", "18789", 
                "--token", "mytoken123",
                "--force"
            );
            
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> clawEnv = clawPb.environment();
            clawEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH")); // 同样注入环境
            clawEnv.put("HOME", baseDir);
            clawEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            clawEnv.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1");
            clawEnv.put("OPENCLAW_N8N_URL", "http://127.0.0.1:30196/webhook/openclaw");

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
