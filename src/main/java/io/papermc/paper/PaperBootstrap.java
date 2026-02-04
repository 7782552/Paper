package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("⚠️ [Zenix-Emergency] 正在强行恢复全家桶系统...");

            // 1. 强制清理残留进程，归还端口
            System.out.println("🔄 正在清理所有 Node 进程...");
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            } catch (Exception ignored) {}
            Thread.sleep(2000);

            // 2. 启动 n8n (自动化中心 - 30196)
            System.out.println("🚀 正在启动 n8n (Port: 30196)...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196"); 
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 启动 OpenClaw (AI 大脑 - 18789)
            // 采用环境变量注入方式，彻底解决 "unknown option --port" 报错
            System.out.println("🧠 正在启动 OpenClaw (Port: 18789)...");
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js");
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 🚨 核心环境变量注入 ---
            cEnv.put("PORT", "18789"); 
            cEnv.put("OPENCLAW_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google"); 
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); // 👈 爹！这里换成你的 Key
            // -------------------------

            clawPb.inheritIO();
            clawPb.start();

            System.out.println("✅ 所有系统已进入启动序列！");
            System.out.println("🌍 n8n 控制台: https://8.8855.cc.cd");
            
            // 保持 Java 进程存活
            while(true) {
                Thread.sleep(60000);
            }

        } catch (Exception e) {
            System.err.println("❌ 严重错误：");
            e.printStackTrace();
        }
    }
}
