package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("⚠️ [Zenix-Emergency] 正在执行全量环境注入启动...");

            // 1. 暴力清理，确保端口 30196 和 18789 彻底空出
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            } catch (Exception ignored) {}
            Thread.sleep(2000);

            // 2. 启动 n8n (自动化中心)
            System.out.println("🚀 启动 n8n...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196"); 
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 启动 OpenClaw (AI 大脑)
            // 核心教训：不再加任何 --参数，只运行 gateway 指令，配置全靠环境变量
            System.out.println("🧠 启动 OpenClaw (API/Gateway 模式)...");
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 🚨 2026 版核心环境变量 (取代所有命令行参数) ---
            cEnv.put("PORT", "18789");                       // 监听端口
            cEnv.put("OPENCLAW_TOKEN", "mytoken123");         // 访问令牌
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");       // 指定 Gemini
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); // 👈 填入你的 Key
            
            // 额外安全补丁：允许 HTTP 访问，防止 405/协议拦截
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            cEnv.put("OPENCLAW_API_PREFIX", "/v1"); 
            // ----------------------------------------------

            clawPb.inheritIO();
            clawPb.start();

            System.out.println("✅ [胜利时刻] 系统已就绪！");
            System.out.println("🔗 n8n 管理页: https://8.8855.cc.cd");
            
            while(true) { Thread.sleep(60000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
