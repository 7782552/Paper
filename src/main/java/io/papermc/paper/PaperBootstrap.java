package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";

        try {
            System.out.println("⚠️ [Zenix-Legacy-Sync] 切换至生产兼容模式启动...");

            // 1. 清理进程
            try { new ProcessBuilder("pkill", "-9", "node").start().waitFor(); } catch (Exception ignored) {}
            Thread.sleep(2000);

            // 2. 启动 n8n (保持不变)
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", baseDir + "/node_modules/.bin/n8n", "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw (核心变动)
            // 使用 prod 模式启动，这会加载完整的 Web 和 API 堆栈
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js", "prod");
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PORT", "18789");
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); 
            
            // 🚨 这一行是解决 405 的救命稻草：强制开启 OpenAI 仿真层
            cEnv.put("OPENCLAW_ENABLE_OPENAI_ADAPTER", "true"); 
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");
            
            clawPb.inheritIO().start();
            System.out.println("✅ 系统已重新校准，请重启后测试。");
            while(true) { Thread.sleep(60000); }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
