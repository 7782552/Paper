package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        
        // 🚨 爹，这里已经帮你改成了你刚解析的纯净域名
        String myDomain = "8.8855.cc.cd"; 

        try {
            System.out.println("🚀 [Zenix-Pure-Direct] 正在绑定纯净域名 " + myDomain + " ...");

            // 1. 强制清理
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Thread.sleep(1000);

            // 2. 启动 N8N (全内存、解除安全 Cookie、绑定域名)
            ProcessBuilder n8nPb = new ProcessBuilder(baseDir + "/node_modules/.bin/n8n", "start");
            
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196"); 
            n8nEnv.put("N8N_LISTEN_ADDRESS", "0.0.0.0");
            
            // 🚨 关键：让 N8N 所有的 Webhook 节点都自动生成这个域名的链接
            n8nEnv.put("WEBHOOK_URL", "http://" + myDomain + ":30196/");
            n8nEnv.put("N8N_SECURE_COOKIE", "false"); 
            n8nEnv.put("N8N_SKIP_WEBHOOK_SELF_CHECK", "true");

            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            Thread.sleep(12000);

            // 3. 启动 OpenClaw
            System.out.println("✅ 域名绑定成功，现在启动 OpenClaw 对接机器人...");
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

            pClaw.waitFor();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
