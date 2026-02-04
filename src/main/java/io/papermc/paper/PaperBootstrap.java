package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("⚠️ [Zenix-Final-Strike] 正在执行最后一次总攻启动...");

            // 1. 暴力清理旧进程
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            } catch (Exception ignored) {}
            Thread.sleep(2000);

            // 2. 启动 n8n (30196)
            System.out.println("🚀 启动 n8n...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196"); 
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 启动 OpenClaw (18789)
            // 修正：删除所有前缀，强制开启实验性 HTTP 接口以解决 405
            System.out.println("🧠 启动 OpenClaw (核心模式)...");
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 🚨 针对 405 错误的终极环境变量注入 ---
            cEnv.put("PORT", "18789"); 
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123"); 
            cEnv.put("OPENCLAW_AI_PROVIDER", "google"); 
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ"); // 👈 爹！填入 Key
            
            // 路由修正核心：
            cEnv.put("OPENCLAW_API_PREFIX", "");           // 强制清空路径前缀
            cEnv.put("OPENCLAW_EXPERIMENTAL_HTTP_API", "true"); // 强制激活 POST 接口
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            // ----------------------------------------

            clawPb.inheritIO();
            clawPb.start();

            System.out.println("✅ [最后部署] 系统已全速运转！");
            System.out.println("🔗 n8n 管理页: https://8.8855.cc.cd");
            
            while(true) { Thread.sleep(60000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
