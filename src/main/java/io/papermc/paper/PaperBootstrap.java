package io.papermc.paper;

import java.io.*;
import java.util.Map;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class PaperBootstrap {
    public static void main(String[] args) {
        // --- 1. 严格对齐您 class 文件中的路径 ---
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin"; // 修正为您的 class 路径
        String nodeBin = nodeBinDir + "/node";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";
        String ocStateDir = baseDir + "/.openclaw";

        try {
            System.out.println("💎 [System-Fusion] 正在初始化 2026 联动环境 (含 Telegram 修复)...");

            // --- 2. 预清理与目录初始化 ---
            File stateDir = new File(ocStateDir);
            if (!stateDir.exists()) stateDir.mkdirs();
            Files.write(Paths.get(ocStateDir, ".onboarded"), "true".getBytes(StandardCharsets.UTF_8));

            // --- 3. 核心：OpenClaw 2026 扁平化 JSON 配置 ---
            // 修正 Telegram 配置：新版通常在 JSON 中定义或通过环境变量覆盖
            String configContent = "{\n" +
                "  \"gateway\": {\n" +
                "    \"address\": \"127.0.0.1\",\n" +
                "    \"port\": 18789,\n" +
                "    \"authEnabled\": false\n" +
                "  }\n" +
                "}";
            Files.write(Paths.get(ocStateDir, "openclaw.json"), configContent.getBytes(StandardCharsets.UTF_8));

            // --- 4. 启动 n8n ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196");
            n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();
            System.out.println("✅ n8n 引擎已就绪: https://8.8855.cc.cd");

            // --- 5. 启动 OpenClaw (重点修复 Telegram 环境变量) ---
            System.out.println("🚀 正在激活 OpenClaw 并挂载 Telegram 模块...");
            ProcessBuilder ocPb = new ProcessBuilder(nodeBin, ocBin, "gateway", "--force");
            
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            ocEnv.put("OPENCLAW_STATE_DIR", ocStateDir);
            ocEnv.put("OPENCLAW_ONBOARDED", "true");

            // --- 修正后的 2026 版环境变量命名 ---
            // 1. Telegram 配置
            ocEnv.put("OPENCLAW_TELEGRAM_ENABLED", "true");
            ocEnv.put("OPENCLAW_TELEGRAM_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            
            // 2. AI 模型配置 (Google Gemini)
            ocEnv.put("OPENCLAW_AI_ENABLED", "true");
            ocEnv.put("OPENCLAW_AI_PROVIDER", "google");
            ocEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");

            ocPb.inheritIO().start();
            System.out.println("✅ OpenClaw 服务已启动，正在尝试连接 Telegram...");

            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
