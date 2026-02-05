package io.papermc.paper; // 必须加上这个包名，匹配你的 Jar 包清单

import java.io.File;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        // 严格匹配你服务器上的 Node 文件夹路径
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String nodePath = nodeBinDir + "/node";

        try {
            System.out.println("🚀 [Zenix-Standard] 正在启动服务并修复 521 访问问题...");

            // 1. 彻底清理端口占用
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
                Thread.sleep(1000L);
            } catch (Exception ignored) {}

            // 2. 启动 n8n
            ProcessBuilder n8nPb = new ProcessBuilder(nodePath, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 核心修复：解决 521 访问拒绝 ---
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("N8N_HOST", "0.0.0.0");               // 允许所有 IP 访问
            nEnv.put("N8N_LISTEN_ADDRESS", "0.0.0.0");     // 强制监听外部请求
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            nEnv.put("N8N_PROTOCOL", "https");
            nEnv.put("N8N_USER_FOLDER", baseDir + "/.n8n"); // 确保数据库有权写入
            // ----------------------------------

            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw (API Gateway 模式)
            System.out.println("🧠 正在同步启动 OpenClaw...");
            ProcessBuilder clawPb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("PORT", "18789");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            clawPb.inheritIO().start();

            System.out.println("✅ 服务已全部就绪！请在 10 秒后刷新网页。");

            while (true) { Thread.sleep(60000L); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
