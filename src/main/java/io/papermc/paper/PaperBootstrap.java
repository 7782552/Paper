package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

        try {
            System.out.println("🚀 [全系统启动] 正在同时拉起 N8N 和 OpenClaw...");

            // 1. 彻底清理环境
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            new ProcessBuilder("pkill", "-9", "n8n").start().waitFor();

            // 2. 【核心】手动启动 N8N 并强制它监听 30196
            // 注意：这里假设 n8n 在你的环境变量里，或者在 node_modules 里
            ProcessBuilder n8nPb = new ProcessBuilder(
                "n8n", "start", "--port", "30196"
            );
            // 如果 n8n 是通过 npm 安装的，可能需要指定路径，如 baseDir + "/node_modules/.bin/n8n"
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();
            System.out.println("✅ N8N 启动指令已发出，目标端口: 30196");

            // 3. 启动 OpenClaw (后台模式，监听内部端口 18789)
            ProcessBuilder clawPb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", "18789",
                "--token", "mytoken123",
                "--force"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> clawEnv = clawPb.environment();
            clawEnv.put("HOME", baseDir);
            clawEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            clawEnv.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1");
            clawEnv.put("OPENCLAW_N8N_URL", "http://127.0.0.1:30196/webhook/openclaw");
            
            clawPb.inheritIO();
            Process pClaw = clawPb.start();

            // 4. 自动审批
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
