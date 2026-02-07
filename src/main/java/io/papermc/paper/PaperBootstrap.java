package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    
    // ========== 改这里 ==========
    static String geminiApiKey = "AIzaSyA1JaNDulB79qXkXfVM2OADeioFQeP02MU";
    static String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
    static String model = "google/gemini-1.5-flash";
    // ============================
    
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw + N8N] 启动中...");
        
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("GEMINI_API_KEY", geminiApiKey);

            // 删除 Webhook
            System.out.println("🗑️ 删除 Telegram Webhook...");
            URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();

            // 删除旧配置
            System.out.println("🧹 删除旧配置...");
            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) {
                deleteDirectory(openclawDir);
                System.out.println("   ✅ 已删除");
            }
            Thread.sleep(1000);

            // 运行 onboard
            System.out.println("📝 运行 onboard...");
            ProcessBuilder onboardPb = new ProcessBuilder(
                nodeBin, ocBin, "onboard",
                "--non-interactive", "--accept-risk",
                "--mode", "local",
                "--auth-choice", "gemini-api-key",
                "--gemini-api-key", geminiApiKey,
                "--gateway-port", "18789",
                "--gateway-bind", "lan",
                "--gateway-auth", "token",
                "--gateway-token", "admin123",
                "--skip-daemon", "--skip-channels",
                "--skip-skills", "--skip-health", "--skip-ui"
            );
            onboardPb.environment().putAll(env);
            onboardPb.directory(new File(baseDir));
            onboardPb.inheritIO();
            onboardPb.start().waitFor();
            Thread.sleep(2000);

            // 写入完整配置（包含 gateway.mode）
            System.out.println("📝 写入配置...");
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            String config = createConfig(model, telegramToken);
            Files.write(configFile.toPath(), config.getBytes());
            System.out.println("   ✅ 模型: " + model);

            // 启动 n8n
            System.out.println("🚀 启动 n8n (端口 30196)...");
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
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();
            
            Thread.sleep(8000);

            // 启动 Gateway
            System.out.println("🚀 启动 OpenClaw Gateway...");
            System.out.println("");
            System.out.println("═".repeat(50));
            System.out.println("🎉 启动完成！");
            System.out.println("═".repeat(50));
            System.out.println("📌 模型: " + model);
            System.out.println("🤖 Telegram Bot: @claw_test_008_bot");
            System.out.println("🌐 n8n: http://你的IP:30196");
            System.out.println("═".repeat(50));

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

    static String createConfig(String modelName, String botToken) {
        // 完整配置，包含所有必要字段
        return "{\n" +
            "  \"meta\": {\n" +
            "    \"lastTouchedVersion\": \"2026.2.3-1\",\n" +
            "    \"lastTouchedAt\": \"" + java.time.Instant.now().toString() + "\"\n" +
            "  },\n" +
            "  \"wizard\": {\n" +
            "    \"lastRunAt\": \"" + java.time.Instant.now().toString() + "\",\n" +
            "    \"lastRunVersion\": \"2026.2.3-1\",\n" +
            "    \"lastRunCommand\": \"onboard\",\n" +
            "    \"lastRunMode\": \"local\"\n" +
            "  },\n" +
            "  \"auth\": {\n" +
            "    \"profiles\": {\n" +
            "      \"google:default\": {\n" +
            "        \"provider\": \"google\",\n" +
            "        \"mode\": \"api_key\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"agents\": {\n" +
            "    \"defaults\": {\n" +
            "      \"model\": {\n" +
            "        \"primary\": \"" + modelName + "\"\n" +
            "      },\n" +
            "      \"workspace\": \"/home/container/.openclaw/workspace\",\n" +
            "      \"compaction\": { \"mode\": \"safeguard\" },\n" +
            "      \"maxConcurrent\": 4,\n" +
            "      \"subagents\": { \"maxConcurrent\": 8 }\n" +
            "    }\n" +
            "  },\n" +
            "  \"messages\": { \"ackReactionScope\": \"group-mentions\" },\n" +
            "  \"commands\": { \"native\": \"auto\", \"nativeSkills\": \"auto\" },\n" +
            "  \"channels\": {\n" +
            "    \"telegram\": {\n" +
            "      \"dmPolicy\": \"open\",\n" +
            "      \"botToken\": \"" + botToken + "\",\n" +
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
            "    },\n" +
            "    \"tailscale\": {\n" +
            "      \"mode\": \"off\",\n" +
            "      \"resetOnExit\": false\n" +
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
