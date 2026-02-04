package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";

        try {
            System.out.println("⚠️ [Zenix-Pure-Start] 正在执行无指令纯净启动...");

            // 1. 暴力清理旧进程
            try { new ProcessBuilder("pkill", "-9", "node").start().waitFor(); } catch (Exception ignored) {}
            Thread.sleep(2000);

            // 2. 启动 n8n
            System.out.println("🚀 启动 n8n...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", baseDir + "/node_modules/.bin/n8n", "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw (核心变动：删掉所有子命令)
            System.out.println("🧠 启动 OpenClaw (原始 index 模式)...");
            // 🚨 关键：不再加 gateway，不再加 prod，直接 node index.js
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js");
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 🚨 核心环境变量注入 ---
            cEnv.put("PORT", "18789");
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); // 👈 爹！这里填 Key
            
            // 解决 405 的终极补丁
            cEnv.put("OPENCLAW_ENABLE_OPENAI_ADAPTER", "true"); 
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            // ----------------------------------------------

            clawPb.inheritIO().start();

            System.out.println("✅ [部署完成] 原始模式已拉起，请观察日志输出。");
            
            while(true) { Thread.sleep(60000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
