package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";

        try {
            System.out.println("⚠️ [Zenix-Final-Resolve] 正在按官方手册强制拉起网关...");

            // 1. 暴力清理旧进程
            try { new ProcessBuilder("pkill", "-9", "node").start().waitFor(); } catch (Exception ignored) {}
            Thread.sleep(2000);

            // 2. 启动 n8n
            System.out.println("🚀 启动 n8n (30196)...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", baseDir + "/node_modules/.bin/n8n", "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw (严格遵守日志里的 Examples)
            System.out.println("🧠 启动 OpenClaw Gateway (强制模式)...");
            // 🚨 核心修正：使用 gateway --force 确保端口被强制接管并启动服务
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node", 
                "dist/index.js", 
                "gateway", 
                "--force", 
                "--port", 
                "18789"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 🚨 环境变量补丁 ---
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); // 👈 爹！填入你的 Key
            
            // 开启 OpenAI 兼容层，这是处理 n8n HTTP 请求的桥梁
            cEnv.put("OPENCLAW_ENABLE_OPENAI_ADAPTER", "true"); 
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            // ----------------------------------------------

            clawPb.inheritIO().start();

            System.out.println("✅ [指令下达] 网关正在强制启动，请观察是否出现 listening...");
            
            while(true) { Thread.sleep(60000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
