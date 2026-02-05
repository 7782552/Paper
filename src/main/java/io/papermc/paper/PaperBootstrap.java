package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBin = baseDir + "/node-v22/bin/node";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";

        try {
            System.out.println("🧪 [System-Fusion] 正在基于源码原理执行环境重构...");

            // --- 第一步：启动 n8n (完全保留你的原始配置) ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, n8nBin, "start");
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.environment().put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.environment().put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            n8nPb.inheritIO().start();

            // --- 第二步：启动 OpenClaw (核心：利用环境变量劫持原理) ---
            // 针对你提供的源码逻辑，我们必须同时注入 PROVIDER 和 MODEL
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", 
                "--allow-unconfigured", 
                "--port", "18789"
            );
            
            Map<String, String> env = ocPb.environment();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            
            // 2026 官方推荐的最强强制变量名 (覆盖 defaults.js 的硬编码)
            String myKey = "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ";
            env.put("OPENCLAW_AI_PROVIDER", "google");
            env.put("OPENCLAW_AI_MODEL", "google/gemini-1.5-pro-latest");
            env.put("OPENCLAW_AI_GOOGLE_API_KEY", myKey);
            env.put("GOOGLE_API_KEY", myKey); // 兼容某些插件直接读取这个变量
            
            // 屏蔽 Telegram，防止它因为找不到 Token 报错
            env.put("OPENCLAW_TELEGRAM_ENABLED", "false");
            env.put("OPENCLAW_GATEWAY_TOKEN", "admin123");

            ocPb.inheritIO().start();
            System.out.println("🚀 环境已重构，OpenClaw 现已强制路由至 Google Gemini。");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
