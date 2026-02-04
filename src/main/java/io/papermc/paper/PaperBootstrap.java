package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("⚠️ [Zenix-Direct-API] 正在执行全量 API 强制挂载启动...");

            // 1. 清理旧进程
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            } catch (Exception ignored) {}
            Thread.sleep(2000);

            // 2. 启动 n8n
            System.out.println("🚀 启动 n8n (30196)...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196"); 
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 启动 OpenClaw (终极逻辑修正)
            // 重点：使用 gateway 模式但强制环境变量前缀，这是目前最稳的 OpenAI 兼容模式启动法
            System.out.println("🧠 启动 OpenClaw (API 模式)...");
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 🚨 消除 405 的核心环境变量 ---
            cEnv.put("PORT", "18789"); 
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123"); 
            cEnv.put("OPENCLAW_AI_PROVIDER", "google"); 
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); // 👈 爹！填入 Key
            
            // 解决路由问题的关键：强制让 API 暴露在 /v1 根目录下
            cEnv.put("OPENCLAW_API_PREFIX", "/v1"); 
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            cEnv.put("OPENCLAW_EXPERIMENTAL_HTTP_API", "true"); 
            // ----------------------------------------------

            clawPb.inheritIO();
            clawPb.start();

            System.out.println("✅ [部署完成] 请去 n8n 尝试最后一次 HTTP 请求！");
            
            while(true) { Thread.sleep(60000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
