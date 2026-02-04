package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("🔄 [Zenix-Restore] 正在还原 n8n 网页环境...");

            // 1. 彻底杀掉冲突进程
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Thread.sleep(1000);

            // 2. 启动 n8n (重新夺回 30196 端口)
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196"); // 你的网页大门
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 启动 OpenClaw (换个后台端口 18789，不跟 n8n 抢地盘)
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node", "dist/index.js", "gateway", 
                "--port", "18789", 
                "--token", "mytoken123", 
                "--force"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            
            clawPb.inheritIO();
            Process pClaw = clawPb.start();

            // 4. 自动审批
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

            System.out.println("✅ n8n 网页已尝试恢复！请刷新 https://8.8855.cc.cd");
            pClaw.waitFor();

        } catch (Exception e) { e.printStackTrace(); }
    }
}
