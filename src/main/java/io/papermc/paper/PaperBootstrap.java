package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String fullNodePath = nodeBinDir + "/node";

        try {
            System.out.println("🚀 [Zenix-Full-Stack] 启动双端口模式：n8n(30196) + 疑似控制台(30195)...");

            // 1. 强力清场，释放所有潜在占用
            try { new ProcessBuilder("pkill", "-9", "node").start().waitFor(); } catch (Exception ignored) {}
            Thread.sleep(3000);

            // 2. 启动 n8n (锁定 30196)
            ProcessBuilder n8nPb = new ProcessBuilder(fullNodePath, baseDir + "/node_modules/.bin/n8n", "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196"); 
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw (API 18789 + Dashboard 30195)
            System.out.println("🧠 正在尝试激活 OpenClaw 接口与控制台...");
            ProcessBuilder clawPb = new ProcessBuilder(
                fullNodePath, "dist/index.js", "gateway", "--force", "--port", "18789"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 端口适配策略 ---
            // 尝试将控制台挂载在 30195
            cEnv.put("OPENCLAW_DASHBOARD_PORT", "30195"); 
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); 
            
            // 解决 n8n 405 的关键适配器
            cEnv.put("OPENCLAW_ENABLE_OPENAI_ADAPTER", "true"); 
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            // ------------------

            clawPb.inheritIO().start();
            System.out.println("✅ 系统已全量拉起。");
            System.out.println("🔗 n8n: 30196 | 控制台探测: 30195");
            
            while(true) { Thread.sleep(60000); }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
