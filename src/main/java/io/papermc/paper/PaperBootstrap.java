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
            System.out.println("🛡️ [Zenix-Shield] 正在强制重置环境以修复打不开的问题...");

            // 1. 暴力清理：不仅杀 node，还尝试释放端口（如果是 Linux 环境）
            try {
                System.out.println("🧹 正在清理可能残留的进程...");
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
                // 尝试杀掉占用 30196 端口的幽灵进程
                new ProcessBuilder("fuser", "-k", "30196/tcp").start().waitFor();
                Thread.sleep(2000L); 
            } catch (Exception ignored) {}

            // 2. 启动 n8n
            ProcessBuilder n8nPb = new ProcessBuilder(nodePath, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // 解决 521 的核心配置
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("N8N_HOST", "0.0.0.0");
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            nEnv.put("N8N_PROTOCOL", "https");
            
            // 关键：如果你之前运行了别的代码，可能导致数据库损坏。
            // 这里强制指定一个新的子目录来尝试启动，或者确保权限。
            nEnv.put("N8N_USER_FOLDER", baseDir + "/.n8n");
            
            // 针对 Cloudflare 的额外优化
            nEnv.put("N8N_PROXY_HOPS", "1"); 

            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw
            System.out.println("🧠 启动 OpenClaw...");
            ProcessBuilder clawPb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("PORT", "18789");
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            clawPb.inheritIO().start();

            System.out.println("✅ 环境已重置，服务已重新拉起。");
            while (true) { Thread.sleep(60000L); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
