package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.time.Instant;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw + n8n] 正在配置 Kimi K2.5 环境 (对象结构修正版)...");
        
        // ================= 配置区 =================
        String kimiApiKey = "sk-0xhxDn6GU2BliEzpLuegxhYc9PL9apvHkEfa1ZEvrZrt43jo"; 
        String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String baseDir = "/home/container";
        // =========================================

        try {
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("MOONSHOT_API_KEY", kimiApiKey);

            // 1. 清理
            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) deleteDirectory(openclawDir);
            new File(baseDir + "/.openclaw/workspace").mkdirs();

            // 2. 写入 2.6.3 严格格式的 openclaw.json
            System.out.println("📝 写入修正后的配置文件 (Object Mode)...");
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            
            // 关键修正：models 数组现在必须是对象列表 [{"id": "..."}]
            String config = "{\n" +
                "  \"meta\": { \"lastTouchedVersion\": \"2026.2.3\", \"lastTouchedAt\": \"" + Instant.now().toString() + "\" },\n" +
                "  \"models\": {\n" +
                "    \"mode\": \"merge\",\n" +
                "    \"providers\": {\n" +
                "      \"moonshot\": {\n" + 
                "        \"baseUrl\": \"https://api.moonshot.cn/v1\",\n" +
                "        \"apiKey\": \"" + kimiApiKey + "\",\n" +
                "        \"api\": \"openai-responses\",\n" +
                "        \"models\": [\n" +
                "          { \"id\": \"kimi-k2.5\" },\n" +
                "          { \"id\": \"moonshot-v1-8k\" }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"agents\": {\n" +
                "    \"defaults\": {\n" +
                "      \"model\": { \"primary\": \"moonshot/kimi-k2.5\" },\n" +
                "      \"workspace\": \"" + baseDir + "/.openclaw/workspace\",\n" +
                "      \"maxConcurrent\": 2\n" +
                "    }\n" +
                "  },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": { \"botToken\": \"" + telegramToken + "\", \"allowFrom\": [\"*\"] }\n" +
                "  },\n" +
                "  \"plugins\": {\n" +
                "    \"entries\": {\n" +
                "      \"telegram\": { \"enabled\": true },\n" +
                "      \"n8n\": { \"enabled\": true },\n" +
                "      \"openai\": { \"enabled\": true }\n" +
                "    }\n" +
                "  }\n" +
                "}";
            
            Files.write(configFile.toPath(), config.getBytes());

            // 3. 启动 n8n (完全保留原始逻辑)
            System.out.println("🚀 启动 n8n...");
            ProcessBuilder n8nPb = new ProcessBuilder(
                nodeBin, "--max-old-space-size=2048",
                baseDir + "/node_modules/.bin/n8n", "start"
            );
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.environment().put("N8N_USER_FOLDER", baseDir + "/.n8n");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();
            
            Thread.sleep(5000);

            // 4. 启动 OpenClaw Gateway
            System.out.println("🚀 启动 OpenClaw Gateway...");
            ProcessBuilder gatewayPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", "--port", "18789", "--token", "admin123", "--verbose"
            );
            gatewayPb.environment().putAll(env);
            gatewayPb.directory(new File(baseDir));
            gatewayPb.inheritIO();
            gatewayPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) deleteDirectory(file);
                else file.delete();
            }
        }
        dir.delete();
    }
}
