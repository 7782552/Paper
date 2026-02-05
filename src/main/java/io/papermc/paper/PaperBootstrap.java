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
            System.out.println("💎 [System-Fusion] 正在激活 Telegram 机器人：@claw_test_008_bot");

            // --- 0. 状态清理 ---
            File stateDir = new File(ocStateDir);
            if (!stateDir.exists()) stateDir.mkdirs();
            // 必须删除旧 JSON，否则会报 Unrecognized key
            Files.deleteIfExists(Paths.get(ocStateDir, "openclaw.json"));
            // 写入已初始化标记
            Files.write(Paths.get(ocStateDir, ".onboarded"), "true".getBytes(StandardCharsets.UTF_8));

            // --- 1. 启动 n8n ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196");
            n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();
            System.out.println("✅ n8n 引擎已就绪");

            // --- 2. 启动 OpenClaw (使用 --allow-unconfigured 绕过配置检查) ---
            // 针对 d84eb46 版本，不再传 --host，仅保留端口和豁免参数
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", 
                "--allow-unconfigured", 
                "--port", "18789"
            );
            
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            ocEnv.put("OPENCLAW_STATE_DIR", ocStateDir);
            
            // --- 核心配置：通过环境变量注入 ---
            // 1. 基础运行模式
            ocEnv.put("OPENCLAW_GATEWAY_MODE", "local");
            ocEnv.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0"); // 允许 n8n 连接
            ocEnv.put("OPENCLAW_GATEWAY_AUTH", "false");

            // 2. Telegram 配置 (你的新 Token)
            ocEnv.put("OPENCLAW_TELEGRAM_ENABLED", "true");
            ocEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            
            // 3. AI 配置 (Gemini)
            ocEnv.put("OPENCLAW_AI_PROVIDER", "google");
            ocEnv.put("OPENCLAW_AI_GOOGLE_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");

            ocPb.inheritIO().start();
            System.out.println("🚀 OpenClaw 已开启「豁免模式」，正在建立与 Telegram 的长连接...");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
