package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        // 🚨 爹！这里就是给机器人装脑子的地方！
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String geminiKey = "这里填入你的_GEMINI_API_KEY"; // <--- 填入你的 Key

        try {
            System.out.println("🧠 [Zenix-AI-Full] 正在启动 n8n 并为 OpenClaw 安装大脑...");

            // 1. 清理战场
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Thread.sleep(1000);

            // 2. 启动 n8n (网页后台，端口 30196)
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196"); 
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            // 3. 启动 OpenClaw (作为 AI 处理引擎)
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node", "dist/index.js", "gateway", 
                "--port", "18789", 
                "--token", "mytoken123", 
                "--force"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // 🧠 脑子配置区：注入 AI 动力
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", geminiKey);
            cEnv.put("OPENCLAW_AI_MODEL", "gemini-1.5-flash"); // 用最灵敏的模型

            clawPb.inheritIO();
            clawPb.start();

            System.out.println("✅ 网页已恢复，大脑已装好！刷新网页并在 n8n 里连线即可。");
            
            while(true) { Thread.sleep(10000); }

        } catch (Exception e) { e.printStackTrace(); }
    }
}
