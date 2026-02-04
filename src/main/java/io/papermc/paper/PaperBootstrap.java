package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";

        try {
            System.out.println("⚠️ [Zenix-Atomic-Final] 正在强制开启双模网关 (WS + HTTP)...");

            // 1. 暴力清理旧进程
            try { new ProcessBuilder("pkill", "-9", "node").start().waitFor(); } catch (Exception ignored) {}
            Thread.sleep(2000);

            // 2. 启动 n8n
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", baseDir + "/node_modules/.bin/n8n", "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw (核心：gateway 后面必须接 api 参数)
            System.out.println("🧠 启动 OpenClaw API 适配层...");
            // 🚨 这一行是解决 405 的唯一解：同时传入 gateway 和 api 命令
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node", 
                "dist/index.js", 
                "gateway", 
                "api",  // 👈 必须加这个，强制拉起 HTTP 路由
                "--force", 
                "--port", 
                "18789"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); 
            
            // 强制环境变量对齐
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            cEnv.put("OPENCLAW_EXPERIMENTAL_HTTP_API", "true"); 
            
            clawPb.inheritIO().start();
            System.out.println("✅ [部署完成] 请观察日志是否出现 [api] listening...");
            
            while(true) { Thread.sleep(60000); }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
