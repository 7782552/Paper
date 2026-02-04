package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";

        try {
            System.out.println("⚠️ [Zenix-Atomic-Correction] 切换官方生产适配模式启动...");

            // 1. 暴力清理旧进程
            try { new ProcessBuilder("pkill", "-9", "node").start().waitFor(); } catch (Exception ignored) {}
            Thread.sleep(2000);

            // 2. 启动 n8n (保持 30196)
            System.out.println("🚀 启动 n8n...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", baseDir + "/node_modules/.bin/n8n", "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw (听官方的，用 prod)
            System.out.println("🧠 启动 OpenClaw (官方 prod 模式)...");
            // 注意：这里换成了 prod 参数
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js", "prod");
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 🚨 官方文档要求的环境变量 (解决 405/路径问题) ---
            cEnv.put("PORT", "18789");
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); // 👈 爹！填入你的 Key
            
            // 关键：强制开启 API 适配层
            cEnv.put("OPENCLAW_ENABLE_OPENAI_ADAPTER", "true"); 
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            // ----------------------------------------------

            clawPb.inheritIO().start();

            System.out.println("✅ [重置完成] 本次启动强制挂载 OpenAI 兼容层。");
            
            while(true) { Thread.sleep(60000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
