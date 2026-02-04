package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("⚠️ [Zenix-Emergency] 启动全家桶：n8n(30196) + OpenClaw(18789)...");

            // 1. 强制清理残留进程，确保端口 18789/30196 必须释放
            System.out.println("🔄 正在清理旧 Node 进程...");
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Thread.sleep(2000);

            // 2. 启动 n8n (自动化中心)
            System.out.println("🚀 正在启动 n8n...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196"); 
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 启动 OpenClaw (核心修改：去掉 gateway，使用完整 API 模式)
            System.out.println("🧠 正在以 API 模式启动 OpenClaw...");
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node", "dist/index.js", 
                "--port", "18789", 
                "--token", "mytoken123", 
                "--force"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 🚨 关键配置补全区 (决定了是否能回信) ---
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google"); 
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); // 👈 爹！填入你的 Key
            // ----------------------------------------

            clawPb.inheritIO();
            clawPb.start();

            System.out.println("✅ 启动完毕！");
            System.out.println("🔗 n8n: https://8.8855.cc.cd");
            
            // 保持主线程
            while(true) { Thread.sleep(60000); }

        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
}
