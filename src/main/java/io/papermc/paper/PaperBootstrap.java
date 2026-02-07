package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.time.Instant;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw + n8n] 正在配置 Kimi K2.5 专项环境...");
        
        // ================= 配置区 =================
        String kimiApiKey = "sk-1Wi9djdIGggsHqPXtKaePhorcmwLt61cCNZqXfox7156UO5k"; 
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

            // 0. 删除 Webhook
            System.out.println("🗑️ 正在重置 Telegram Webhook...");
            try {
                URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.getResponseCode();
            } catch (Exception e) {
                System.out.println("⚠️ Webhook 重置跳过 (网络或Token问题)");
            }

            // 1. 清理 OpenClaw 旧配置 (确保 Kimi 纯净环境)
            System.out.println("🧹 清理旧缓存...");
            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) {
                deleteDirectory(openclawDir);
            }
            new File(baseDir + "/.openclaw/workspace").mkdirs();
            Thread.sleep(1000);

            // 2. 运行 onboard (使用 Kimi 专用参数)
            System.out.println("📝 初始化 Kimi 认证模块...");
            ProcessBuilder onboardPb = new ProcessBuilder(
                nodeBin, ocBin, "onboard",
                "--non-interactive",
                "--accept-risk",
                "--mode", "local",
                "--auth-choice", "moonshot", 
                "--kimi-api-key", kimiApiKey,
                "--gateway-port", "18789",
                "--gateway-bind", "lan",
                "--gateway-auth", "token",
                "--gateway-token", "admin123",
                "--skip-daemon",
                "--skip-ui"
            );
            onboardPb.environment().putAll(env);
            onboardPb.directory(new File(baseDir));
            onboardPb.inheritIO();
            onboardPb.start().waitFor();

            // 3. 写入最终修正版 openclaw.json
            System.out.println("📝 写入 Kimi K2.5 官方推荐配置...");
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            
            String config = "{\n" +
                "  \"meta\": {\n" +
                "    \"lastTouchedVersion\": \"2026.2.3\",\n" +
                "    \"lastTouchedAt\": \"" + Instant.now().toString() + "\"\n" +
                "  },\n" +
                "  \"models\": {\n" +
                "    \"mode\": \"merge\",\n" +
                "    \"providers\": {\n" +
                "      \"moonshot\": {\n" + 
                "        \"baseUrl\": \"https://api.moonshot.cn/v1\",\n" +
                "        \"apiKey\": \"" + kimiApiKey + "\",\n" +
                "        \"api\": \"openai-responses\"\n" +
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
                "    \"telegram\": {\n" +
                "      \"botToken\": \"" + telegramToken + "\",\n" +
                "      \"dmPolicy\": \"open\",\n" +
                "      \"allowFrom\": [\"*\"]\n" +
                "    }\n" +
                "  },\n" +
                "  \"gateway\": {\n" +
                "    \"port\": 18789,\n" +
                "    \"auth\": { \"mode\": \"token\", \"token\": \"admin123\" }\n" +
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

            // 4. 启动 n8n (完全保留原始逻辑)
            System.out.println("\n🚀 启动 n8n 自动化引擎...");
            File n8nDir = new File(baseDir + "/.n8n");
            if (!n8nDir.exists()) n8nDir.mkdirs();

            ProcessBuilder n8nPb = new ProcessBuilder(
                nodeBin, "--max-old-space-size=2048",
                baseDir + "/node_modules/.bin/n8n", "start"
            );
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.environment().put("N8N_HOST", "0.0.0.0");
            n8nPb.environment().put("N8N_SECURE_COOKIE", "false");
            n8nPb.environment().put("N8N_USER_FOLDER", baseDir + "/.n8n");
            n8nPb.environment().put("N8N_DIAGNOSTICS_ENABLED", "false");
            n8nPb.environment().put("N8N_VERSION_NOTIFICATIONS_ENABLED", "false");
            n8nPb.environment().put("N8N_HIRING_BANNER_ENABLED", "false");
            n8nPb.environment().put("N8N_PERSONALIZATION_ENABLED", "false");
            n8nPb.environment().put("N8N_TEMPLATES_ENABLED", "false");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();
            
            System.out.println("⏳ 等待 n8n 启动 (8s)...");
            Thread.sleep(8000);

            // 5. 启动 OpenClaw Gateway
            System.out.println("\n🚀 启动 OpenClaw Gateway (Kimi 模式)...");
            ProcessBuilder gatewayPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway",
                "--port", "18789",
                "--token", "admin123",
                "--verbose"
            );
            gatewayPb.environment().putAll(env);
            gatewayPb.directory(new File(baseDir));
            gatewayPb.inheritIO();
            gatewayPb.start().waitFor();

        } catch (Exception e) {
            System.err.println("❌ 运行出错: " + e.getMessage());
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
