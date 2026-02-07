package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 正在配置 Kimi K2.5...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            String kimiApiKey = "sk-Smr4THSBZfy8J4FEmIoLnTtfHovPx8BUzOGct1g2pRRxkY3Z";  // ← 换成你的真实 API Key
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("MOONSHOT_API_KEY", kimiApiKey);

            // 0. 删除 Webhook
            System.out.println("🗑️ 删除 Telegram Webhook...");
            URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();

            // 1. 删除旧配置
            System.out.println("🧹 删除旧配置...");
            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) {
                deleteDirectory(openclawDir);
            }
            openclawDir.mkdirs();
            Thread.sleep(1000);

            // 2. 写入配置 - 移除 api 字段，让 OpenClaw 自动检测
            System.out.println("📝 写入 Kimi K2.5 配置...");
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            
            String config = "{\n" +
                "  \"meta\": {\n" +
                "    \"lastTouchedVersion\": \"2026.2.3-1\",\n" +
                "    \"lastTouchedAt\": \"" + java.time.Instant.now().toString() + "\"\n" +
                "  },\n" +
                "  \"models\": {\n" +
                "    \"mode\": \"merge\",\n" +
                "    \"providers\": {\n" +
                "      \"moonshot\": {\n" +
                "        \"baseUrl\": \"https://api.moonshot.cn/v1\",\n" +
                "        \"apiKey\": \"" + kimiApiKey + "\",\n" +
                "        \"models\": [\n" +
                "          { \"id\": \"kimi-k2.5\", \"name\": \"Kimi K2.5\" }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"agents\": {\n" +
                "    \"defaults\": {\n" +
                "      \"model\": {\n" +
                "        \"primary\": \"moonshot/kimi-k2.5\"\n" +
                "      },\n" +
                "      \"workspace\": \"/home/container/.openclaw/workspace\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"dmPolicy\": \"open\",\n" +
                "      \"botToken\": \"" + telegramToken + "\",\n" +
                "      \"groupPolicy\": \"open\",\n" +
                "      \"streamMode\": \"partial\",\n" +
                "      \"allowFrom\": [\"*\"]\n" +
                "    }\n" +
                "  },\n" +
                "  \"gateway\": {\n" +
                "    \"port\": 18789,\n" +
                "    \"mode\": \"local\",\n" +
                "    \"bind\": \"lan\",\n" +
                "    \"auth\": {\n" +
                "      \"mode\": \"token\",\n" +
                "      \"token\": \"admin123\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"plugins\": {\n" +
                "    \"entries\": {\n" +
                "      \"telegram\": {\n" +
                "        \"enabled\": true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
            
            Files.write(configFile.toPath(), config.getBytes());
            System.out.println("✅ 配置已写入");

            // 3. 创建 workspace 目录
            new File(baseDir + "/.openclaw/workspace").mkdirs();

            System.out.println("\n📋 模型: moonshot/kimi-k2.5");

            // 4. 启动 n8n
            System.out.println("\n🚀 启动 n8n...");
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
            Thread.sleep(8000);

            // 5. 启动 Gateway
            System.out.println("\n🚀 启动 Gateway...");
            ProcessBuilder gatewayPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway",
                "--port", "18789",
                "--bind", "lan",
                "--token", "admin123",
                "--verbose"
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
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
