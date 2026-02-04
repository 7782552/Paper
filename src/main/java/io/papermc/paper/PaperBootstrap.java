package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

        try {
            System.out.println("🚀 [Zenix-Trinity-Pro] 智能核心修复启动中...");

            // 1. 彻底清理残留 node
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 先拉起 N8N (作为主服务)
            System.out.println("🔥 正在拉起 n8n...");
            ProcessBuilder n8nPb = new ProcessBuilder("n8n", "start", "--port", "30196");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 等待 5 秒，让 N8N 先把端口占稳
            Thread.sleep(5000);

            // 4. 启动 OpenClaw (作为后台插件)
            System.out.println("✅ 正在启动 OpenClaw 后台组件...");
            ProcessBuilder clawPb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", "18789", // 绝对不准抢 30196
                "--token", "mytoken123",
                "--force"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = clawPb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1"); // 只守在内网
            env.put("OPENCLAW_N8N_URL", "http://127.0.0.1:30196/webhook/openclaw"); // 直接内网投喂 N8N

            clawPb.inheritIO();
            Process pClaw = clawPb.start();

            // 5. 自动审批 (保持 10 秒循环)
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(pClaw.getOutputStream()));
            new Thread(() -> {
                try {
                    while (pClaw.isAlive()) {
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
