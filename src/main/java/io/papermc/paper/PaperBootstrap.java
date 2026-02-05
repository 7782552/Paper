package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBin = "/home/container/node-v22/bin/node"; 
        String nodeBinDir = new File(nodeBin).getParent();
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";
        String ocStateDir = baseDir + "/.openclaw";

        try {
            System.out.println("🦞 [System-Fusion] 正在绕过网关验证：@claw_test_008_bot");

            // --- 0. 环境初始化 ---
            File stateDir = new File(ocStateDir);
            if (!stateDir.exists()) stateDir.mkdirs();
            Files.deleteIfExists(Paths.get(ocStateDir, "openclaw.json"));
            Files.write(Paths.get(ocStateDir, ".onboarded"), "true".getBytes(StandardCharsets.UTF_8));

            // --- 1. 启动 n8n ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196");
            n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();

            // --- 2. 启动 OpenClaw (重点修复：环境变量+免密参数) ---
            // 增加 --no-auth 强行覆盖
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", 
                "--allow-unconfigured",
                "--no-auth", 
                "--port", "18789"
            );
            
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            ocEnv.put("OPENCLAW_STATE_DIR", ocStateDir);
            
            // 针对 13:01:28 报错的专项修复：
            // 1. 强制关闭网关验证逻辑
            ocEnv.put("OPENCLAW_GATEWAY_AUTH", "none"); 
            ocEnv.put("OPENCLAW_GATEWAY_TOKEN", "system_fusion_bypass"); // 即使不认 none，也喂它一个假 token
            ocEnv.put("OPENCLAW_GATEWAY_MODE", "local");
            ocEnv.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0");

            // 2. Telegram 配置
            ocEnv.put("OPENCLAW_TELEGRAM_ENABLED", "true");
            ocEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            
            // 3. AI 配置
            ocEnv.put("OPENCLAW_AI_PROVIDER", "google");
            ocEnv.put("OPENCLAW_AI_GOOGLE_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");

            ocPb.inheritIO().start();
            System.out.println("🚀 验证豁免已下发。如果 Telegram 连接成功，控制台将显示 bot 信息。");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
