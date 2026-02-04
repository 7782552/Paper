package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

        try {
            System.out.println("🚀 [Zenix-Ultra-Fix] 正在深度扫描 N8N 路径并尝试启动...");

            // 1. 清理所有残留
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 定义 N8N 所有的可能藏身路径
            String[] n8nCommands = {
                "n8n",                                      // 环境变量
                baseDir + "/node_modules/.bin/n8n",         // 本地 node_modules
                "/usr/local/bin/n8n",                      // 全局 bin
                baseDir + "/.npm-global/bin/n8n",           // 自定义全局
                "node_modules/n8n/bin/n8n"                  // 相对路径
            };

            boolean n8nStarted = false;
            for (String cmd : n8nCommands) {
                try {
                    ProcessBuilder n8nPb = new ProcessBuilder(cmd, "start", "--port", "30196");
                    n8nPb.directory(new File(baseDir));
                    n8nPb.inheritIO();
                    n8nPb.start();
                    System.out.println("✅ 找到并启动了 N8N: " + cmd);
                    n8nStarted = true;
                    break;
                } catch (IOException e) {
                    // 没找到就换下一个路径
                }
            }

            if (!n8nStarted) {
                System.err.println("❌ 依然找不到 n8n 命令。爹，你确定这个容器里装了 n8n 吗？");
            }

            // 3. 稳一手，等 N8N 占坑
            Thread.sleep(3000);

            // 4. 拉起 OpenClaw (内网模式，坚决不抢 30196)
            System.out.println("✅ 正在拉起 OpenClaw 后台助理...");
            ProcessBuilder clawPb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", "18789", 
                "--token", "mytoken123",
                "--force"
            );
            
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = clawPb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1"); // 内网模式，避开 400 错误
            env.put("OPENCLAW_N8N_URL", "http://127.0.0.1:30196/webhook/openclaw");

            clawPb.inheritIO();
            Process pClaw = clawPb.start();

            // 5. 自动审批 (每 10 秒戳一次)
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
