package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

        try {
            System.out.println("🔥 [Zenix-Ultimate] 强制端口重定向启动...");

            // 1. 杀掉旧进程
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 启动 N8N (增加强制端口环境变量)
            System.out.println("📢 强制 N8N 监听 30196...");
            ProcessBuilder n8nPb = new ProcessBuilder(baseDir + "/node_modules/.bin/n8n", "start");
            
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            // 🚨 这里的环境变量是最高优先级，N8N 必须服从
            n8nEnv.put("N8N_PORT", "30196"); 
            n8nEnv.put("N8N_LISTEN_ADDRESS", "0.0.0.0");
            
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 等待 N8N 初始化
            Thread.sleep(8000);

            // 4. 启动 OpenClaw
            System.out.println("✅ 启动 OpenClaw 对接组件...");
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node",
                "dist/index.js", "gateway", 
                "--port", "18789", 
                "--token", "mytoken123",
                "--force"
            );
            
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> clawEnv = clawPb.environment();
            clawEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            clawEnv.put("HOME", baseDir);
            clawEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            clawEnv.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1");
            // 🚨 OpenClaw 现在去 30196 找 N8N
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
