package io.papermc.paper;

import java.io.File;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String nodePath = nodeBinDir + "/node";

        try {
            System.out.println("🛡️ [Zenix-Shield] 正在强制重置 IPv4 环境...");

            // 1. 强力清理所有 Node 进程
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
                Thread.sleep(1500L);
            } catch (Exception ignored) {}

            // 2. 启动 n8n
            ProcessBuilder n8nPb = new ProcessBuilder(nodePath, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 【核心修正：强制 IPv4 和 0.0.0.0】 ---
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("N8N_HOST", "0.0.0.0");               // 显式指定 IPv4 零地址
            nEnv.put("N8N_LISTEN_ADDRESS", "0.0.0.0");     // 双重保险
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            nEnv.put("N8N_PROTOCOL", "https");
            
            // 解决因为运行“别的代码”导致的配置污染
            nEnv.put("N8N_ENFORCE_SETTINGS_FILE_PERMISSIONS", "false");
            // ----------------------------------------

            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw
            System.out.println("🧠 启动 OpenClaw...");
            ProcessBuilder clawPb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("PORT", "18789");
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            clawPb.inheritIO().start();

            System.out.println("✅ 已尝试强制 IPv4 绑定，请刷新页面。");
            while (true) { Thread.sleep(60000L); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
