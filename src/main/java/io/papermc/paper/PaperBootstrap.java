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

            // 1. 强制清理旧进程，防止端口占用
            System.out.println("🔄 正在清理残留 Node 进程...");
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

            // 3. 启动 OpenClaw (AI 脑子)
            System.out.println("🧠 正在启动 OpenClaw...");
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node", "dist/index.js", "gateway", 
                "--port", "18789", 
                "--token", "mytoken123", 
                "--force"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 🚨 关键配置补全区 ---
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google"); // 必须指定提供商
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); // 👈 爹！这里换成你真正的 Gemini Key！
            // -----------------------

            clawPb.inheritIO();
            clawPb.start();

            System.out.println("✅ 启动序列完成！");
            System.out.println("1️⃣ n8n 网页: https://8.8855.cc.cd");
            System.out.println("2️⃣ OpenClaw 接口: 127.0.0.1:18789");
            
            // 保持主线程不退出
            while(true) { Thread.sleep(60000); }

        } catch (Exception e) { 
            System.err.println("❌ 启动失败！错误详情：");
            e.printStackTrace(); 
        }
    }
}
